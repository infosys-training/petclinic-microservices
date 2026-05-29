"use client";

import React, { useCallback, useState } from "react";
import { useDropzone } from "react-dropzone";
import { Upload, File, X, Loader2 } from "lucide-react";
import { Button } from "@/components/ui/button";
import { Badge } from "@/components/ui/badge";
import { api, Document } from "@/lib/api";

interface FileUploadProps {
  projectId: string;
  documents: Document[];
  onUploadComplete: () => void;
}

export function FileUpload({ projectId, documents, onUploadComplete }: FileUploadProps) {
  const [uploading, setUploading] = useState(false);
  const [error, setError] = useState<string | null>(null);

  const onDrop = useCallback(
    async (acceptedFiles: File[]) => {
      setUploading(true);
      setError(null);
      try {
        for (const file of acceptedFiles) {
          await api.uploadDocument(projectId, file);
        }
        onUploadComplete();
      } catch (err) {
        setError(err instanceof Error ? err.message : "Upload failed");
      } finally {
        setUploading(false);
      }
    },
    [projectId, onUploadComplete]
  );

  const { getRootProps, getInputProps, isDragActive } = useDropzone({
    onDrop,
    accept: {
      "application/pdf": [".pdf"],
      "application/vnd.openxmlformats-officedocument.wordprocessingml.document": [".docx"],
      "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet": [".xlsx"],
      "application/vnd.ms-excel": [".xls"],
      "text/plain": [".txt"],
      "image/png": [".png"],
      "image/jpeg": [".jpg", ".jpeg"],
      "image/tiff": [".tiff", ".tif"],
    },
    maxSize: 50 * 1024 * 1024,
  });

  const handleDelete = async (docId: string) => {
    try {
      await api.deleteDocument(projectId, docId);
      onUploadComplete();
    } catch (err) {
      setError(err instanceof Error ? err.message : "Delete failed");
    }
  };

  const formatSize = (bytes: number | null) => {
    if (!bytes) return "N/A";
    if (bytes < 1024) return `${bytes} B`;
    if (bytes < 1024 * 1024) return `${(bytes / 1024).toFixed(1)} KB`;
    return `${(bytes / (1024 * 1024)).toFixed(1)} MB`;
  };

  return (
    <div className="space-y-4">
      <div
        {...getRootProps()}
        className={`border-2 border-dashed rounded-lg p-8 text-center cursor-pointer transition-colors ${
          isDragActive
            ? "border-blue-500 bg-blue-50"
            : "border-gray-300 hover:border-blue-400 hover:bg-gray-50"
        }`}
      >
        <input {...getInputProps()} />
        {uploading ? (
          <div className="flex flex-col items-center">
            <Loader2 className="h-10 w-10 text-blue-500 animate-spin mb-2" />
            <p className="text-sm text-gray-600">Uploading and processing...</p>
          </div>
        ) : (
          <div className="flex flex-col items-center">
            <Upload className="h-10 w-10 text-gray-400 mb-2" />
            <p className="text-sm font-medium text-gray-700">
              {isDragActive ? "Drop files here" : "Drag & drop RFP/RFI documents"}
            </p>
            <p className="text-xs text-gray-500 mt-1">
              PDF, DOCX, XLSX, TXT, or scanned images (max 50MB)
            </p>
          </div>
        )}
      </div>

      {error && (
        <div className="bg-red-50 text-red-700 px-4 py-2 rounded-md text-sm">{error}</div>
      )}

      {documents.length > 0 && (
        <div className="space-y-2">
          <h4 className="text-sm font-medium text-gray-700">Uploaded Documents</h4>
          {documents.map((doc) => (
            <div
              key={doc.id}
              className="flex items-center justify-between bg-gray-50 rounded-lg px-4 py-2"
            >
              <div className="flex items-center gap-3">
                <File className="h-4 w-4 text-gray-500" />
                <div>
                  <p className="text-sm font-medium">{doc.filename}</p>
                  <p className="text-xs text-gray-500">
                    {formatSize(doc.file_size)} &middot; {doc.file_type}
                  </p>
                </div>
                <Badge variant="secondary">{doc.doc_type}</Badge>
              </div>
              <Button
                variant="ghost"
                size="icon"
                onClick={() => handleDelete(doc.id)}
                className="h-8 w-8 text-gray-400 hover:text-red-500"
              >
                <X className="h-4 w-4" />
              </Button>
            </div>
          ))}
        </div>
      )}
    </div>
  );
}
