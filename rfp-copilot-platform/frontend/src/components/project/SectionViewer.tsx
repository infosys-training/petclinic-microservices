"use client";

import React, { useState } from "react";
import ReactMarkdown from "react-markdown";
import { Edit3, RefreshCw, Check, X, Loader2 } from "lucide-react";
import { Button } from "@/components/ui/button";
import { Badge } from "@/components/ui/badge";
import { Progress } from "@/components/ui/progress";
import { Section, api } from "@/lib/api";

interface SectionViewerProps {
  section: Section;
  projectId: string;
  onUpdate: () => void;
}

export function SectionViewer({ section, projectId, onUpdate }: SectionViewerProps) {
  const [editing, setEditing] = useState(false);
  const [editContent, setEditContent] = useState(section.content || "");
  const [regenerating, setRegenerating] = useState(false);
  const [saving, setSaving] = useState(false);

  const handleSave = async () => {
    setSaving(true);
    try {
      await api.updateSection(projectId, section.id, { content: editContent });
      setEditing(false);
      onUpdate();
    } catch (err) {
      console.error("Save failed:", err);
    } finally {
      setSaving(false);
    }
  };

  const handleRegenerate = async () => {
    setRegenerating(true);
    try {
      await api.regenerateSection(projectId, section.tab_key);
      onUpdate();
    } catch (err) {
      console.error("Regeneration failed:", err);
    } finally {
      setRegenerating(false);
    }
  };

  const statusBadge = () => {
    const variants: Record<string, "default" | "secondary" | "success" | "warning" | "destructive"> = {
      pending: "secondary",
      generating: "warning",
      generated: "success",
      edited: "default",
      approved: "success",
    };
    return (
      <Badge variant={variants[section.status] || "secondary"}>
        {section.status}
      </Badge>
    );
  };

  return (
    <div className="space-y-4">
      <div className="flex items-center justify-between">
        <div className="flex items-center gap-3">
          {statusBadge()}
          {section.confidence_score !== null && (
            <div className="flex items-center gap-2">
              <span className="text-xs text-gray-500">Confidence:</span>
              <Progress value={section.confidence_score} className="w-24 h-2" />
              <span className="text-xs font-medium">{section.confidence_score?.toFixed(0)}%</span>
            </div>
          )}
          <span className="text-xs text-gray-400">v{section.version}</span>
        </div>
        <div className="flex items-center gap-2">
          {!editing && section.content && (
            <>
              <Button
                variant="outline"
                size="sm"
                onClick={() => {
                  setEditContent(section.content || "");
                  setEditing(true);
                }}
              >
                <Edit3 className="h-3.5 w-3.5 mr-1" /> Edit
              </Button>
              <Button
                variant="outline"
                size="sm"
                onClick={handleRegenerate}
                disabled={regenerating}
              >
                {regenerating ? (
                  <Loader2 className="h-3.5 w-3.5 mr-1 animate-spin" />
                ) : (
                  <RefreshCw className="h-3.5 w-3.5 mr-1" />
                )}
                Regenerate
              </Button>
            </>
          )}
        </div>
      </div>

      {editing ? (
        <div className="space-y-3">
          <textarea
            value={editContent}
            onChange={(e) => setEditContent(e.target.value)}
            className="w-full h-96 p-4 border border-gray-300 rounded-lg font-mono text-sm resize-y focus:ring-2 focus:ring-blue-500 focus:border-transparent"
          />
          <div className="flex gap-2">
            <Button size="sm" onClick={handleSave} disabled={saving}>
              {saving ? (
                <Loader2 className="h-3.5 w-3.5 mr-1 animate-spin" />
              ) : (
                <Check className="h-3.5 w-3.5 mr-1" />
              )}
              Save
            </Button>
            <Button
              variant="outline"
              size="sm"
              onClick={() => setEditing(false)}
            >
              <X className="h-3.5 w-3.5 mr-1" /> Cancel
            </Button>
          </div>
        </div>
      ) : section.content ? (
        <div className="prose prose-sm max-w-none prose-headings:text-gray-900 prose-p:text-gray-700 prose-strong:text-gray-900 prose-table:text-sm">
          <ReactMarkdown>{section.content}</ReactMarkdown>
        </div>
      ) : (
        <div className="flex flex-col items-center justify-center py-16 text-gray-400">
          <p className="text-sm">No content generated yet.</p>
          <p className="text-xs mt-1">Upload documents and click &ldquo;Generate All Sections&rdquo; to start.</p>
        </div>
      )}

      {section.citations && (
        <div className="bg-blue-50 rounded-lg p-3 mt-4">
          <p className="text-xs font-medium text-blue-800 mb-1">Citations</p>
          <p className="text-xs text-blue-700">{section.citations}</p>
        </div>
      )}
    </div>
  );
}
