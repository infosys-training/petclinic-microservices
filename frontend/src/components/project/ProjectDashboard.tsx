"use client";

import React, { useEffect, useState, useCallback } from "react";
import { Loader2, Download, Sparkles, FileText, Settings } from "lucide-react";
import { Button } from "@/components/ui/button";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { Tabs, TabsContent, TabsList, TabsTrigger } from "@/components/ui/tabs";
import { Badge } from "@/components/ui/badge";
import { FileUpload } from "./FileUpload";
import { SectionViewer } from "./SectionViewer";
import { api, Project, Document, Section } from "@/lib/api";

const TAB_LABELS: Record<string, string> = {
  current_system_challenges: "Current System & Challenges",
  ai_dlc_solution: "AI-DLC Solution",
  estimation: "Estimation",
  target_architecture: "Target Architecture",
  testing_approach: "Testing Approach",
  devops_approach: "DevOps Approach",
  db_rearchitecture: "DB Re-architecture",
  db_migration: "DB Migration",
  user_onboarding: "User Onboarding",
  scope_assumptions_risks: "Scope & Risks",
};

interface ProjectDashboardProps {
  projectId: string;
}

export function ProjectDashboard({ projectId }: ProjectDashboardProps) {
  const [project, setProject] = useState<Project | null>(null);
  const [documents, setDocuments] = useState<Document[]>([]);
  const [sections, setSections] = useState<Section[]>([]);
  const [loading, setLoading] = useState(true);
  const [generating, setGenerating] = useState(false);
  const [exporting, setExporting] = useState(false);
  const [activeTab, setActiveTab] = useState("current_system_challenges");
  const [showContext, setShowContext] = useState(false);
  const [additionalContext, setAdditionalContext] = useState("");
  const [error, setError] = useState<string | null>(null);

  const loadData = useCallback(async () => {
    try {
      const [proj, docs, sects] = await Promise.all([
        api.getProject(projectId),
        api.listDocuments(projectId),
        api.listSections(projectId),
      ]);
      setProject(proj);
      setDocuments(docs);
      setSections(sects.sort((a, b) => a.tab_index - b.tab_index));
      setAdditionalContext(proj.additional_context || "");
    } catch (err) {
      setError(err instanceof Error ? err.message : "Failed to load project");
    } finally {
      setLoading(false);
    }
  }, [projectId]);

  useEffect(() => {
    loadData();
  }, [loadData]);

  const handleGenerate = async (tabKeys?: string[]) => {
    setGenerating(true);
    setError(null);
    try {
      if (additionalContext && project) {
        await api.updateProject(projectId, { additional_context: additionalContext });
      }
      await api.generateSections(projectId, tabKeys, additionalContext);
      await loadData();
    } catch (err) {
      setError(err instanceof Error ? err.message : "Generation failed");
    } finally {
      setGenerating(false);
    }
  };

  const handleExport = async (format: string) => {
    setExporting(true);
    try {
      const blob = await api.exportProject(projectId, format);
      const url = window.URL.createObjectURL(blob);
      const a = document.createElement("a");
      a.href = url;
      a.download = `${project?.name || "RFP_Response"}.${format}`;
      document.body.appendChild(a);
      a.click();
      window.URL.revokeObjectURL(url);
      document.body.removeChild(a);
    } catch (err) {
      setError(err instanceof Error ? err.message : "Export failed");
    } finally {
      setExporting(false);
    }
  };

  if (loading) {
    return (
      <div className="flex items-center justify-center h-64">
        <Loader2 className="h-8 w-8 animate-spin text-blue-600" />
      </div>
    );
  }

  if (!project) {
    return <div className="text-center py-16 text-gray-500">Project not found</div>;
  }

  const generatedCount = sections.filter((s) => s.status === "generated" || s.status === "edited").length;

  return (
    <div className="space-y-6">
      {/* Header */}
      <div className="flex items-start justify-between">
        <div>
          <h1 className="text-2xl font-bold text-gray-900">{project.name}</h1>
          <div className="flex items-center gap-3 mt-2">
            {project.client_name && (
              <Badge variant="outline">{project.client_name}</Badge>
            )}
            {project.industry && (
              <Badge variant="secondary">{project.industry}</Badge>
            )}
            <Badge
              variant={project.status === "completed" ? "success" : "default"}
            >
              {project.status}
            </Badge>
            <span className="text-sm text-gray-500">
              {generatedCount}/{sections.length} sections generated
            </span>
          </div>
        </div>
        <div className="flex gap-2">
          <Button
            variant="outline"
            size="sm"
            onClick={() => setShowContext(!showContext)}
          >
            <Settings className="h-4 w-4 mr-1" /> Context
          </Button>
          <Button
            variant="outline"
            size="sm"
            onClick={() => handleExport("docx")}
            disabled={exporting}
          >
            {exporting ? (
              <Loader2 className="h-4 w-4 mr-1 animate-spin" />
            ) : (
              <Download className="h-4 w-4 mr-1" />
            )}
            Export DOCX
          </Button>
          <Button
            variant="outline"
            size="sm"
            onClick={() => handleExport("pdf")}
            disabled={exporting}
          >
            <FileText className="h-4 w-4 mr-1" /> Export PDF
          </Button>
          <Button
            onClick={() => handleGenerate()}
            disabled={generating || documents.length === 0}
          >
            {generating ? (
              <Loader2 className="h-4 w-4 mr-1 animate-spin" />
            ) : (
              <Sparkles className="h-4 w-4 mr-1" />
            )}
            Generate All Sections
          </Button>
        </div>
      </div>

      {error && (
        <div className="bg-red-50 text-red-700 px-4 py-3 rounded-lg text-sm">{error}</div>
      )}

      {/* Additional Context Panel */}
      {showContext && (
        <Card>
          <CardHeader>
            <CardTitle className="text-lg">Additional Context</CardTitle>
          </CardHeader>
          <CardContent>
            <textarea
              value={additionalContext}
              onChange={(e) => setAdditionalContext(e.target.value)}
              placeholder="Provide additional context: team size, tech stack preferences, client industry specifics, etc."
              className="w-full h-32 p-3 border border-gray-300 rounded-lg text-sm resize-y focus:ring-2 focus:ring-blue-500 focus:border-transparent"
            />
          </CardContent>
        </Card>
      )}

      {/* Document Upload */}
      <Card>
        <CardHeader>
          <CardTitle className="text-lg">Documents</CardTitle>
        </CardHeader>
        <CardContent>
          <FileUpload
            projectId={projectId}
            documents={documents}
            onUploadComplete={loadData}
          />
        </CardContent>
      </Card>

      {/* Tab-based Sections */}
      <Card>
        <CardContent className="pt-6">
          <Tabs value={activeTab} onValueChange={setActiveTab}>
            <TabsList className="w-full justify-start">
              {sections.map((section) => (
                <TabsTrigger
                  key={section.tab_key}
                  value={section.tab_key}
                  className="text-xs"
                >
                  <span className="mr-1.5 font-bold">{section.tab_index + 1}.</span>
                  {TAB_LABELS[section.tab_key] || section.title}
                  {section.status === "generated" && (
                    <span className="ml-1.5 h-1.5 w-1.5 rounded-full bg-green-500 inline-block" />
                  )}
                  {section.status === "generating" && (
                    <Loader2 className="ml-1.5 h-3 w-3 animate-spin" />
                  )}
                </TabsTrigger>
              ))}
            </TabsList>
            {sections.map((section) => (
              <TabsContent key={section.tab_key} value={section.tab_key}>
                <div className="mt-4">
                  <h2 className="text-xl font-semibold text-gray-900 mb-4">
                    {section.title}
                  </h2>
                  <SectionViewer
                    section={section}
                    projectId={projectId}
                    onUpdate={loadData}
                  />
                </div>
              </TabsContent>
            ))}
          </Tabs>
        </CardContent>
      </Card>
    </div>
  );
}
