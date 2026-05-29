const API_BASE = process.env.NEXT_PUBLIC_API_URL || "http://localhost:8000/api";

async function request<T>(path: string, options?: RequestInit): Promise<T> {
  const url = `${API_BASE}${path}`;
  const res = await fetch(url, {
    ...options,
    headers: {
      ...(options?.body instanceof FormData
        ? {}
        : { "Content-Type": "application/json" }),
      ...options?.headers,
    },
  });
  if (!res.ok) {
    const error = await res.json().catch(() => ({ detail: res.statusText }));
    throw new Error(error.detail || `Request failed: ${res.status}`);
  }
  if (res.status === 204) return undefined as T;
  return res.json();
}

export const api = {
  // Projects
  listProjects: () => request<Project[]>("/projects/"),
  getProject: (id: string) => request<Project>(`/projects/${id}`),
  createProject: (data: CreateProjectData) =>
    request<Project>("/projects/", { method: "POST", body: JSON.stringify(data) }),
  updateProject: (id: string, data: Partial<CreateProjectData>) =>
    request<Project>(`/projects/${id}`, { method: "PATCH", body: JSON.stringify(data) }),
  deleteProject: (id: string) =>
    request<void>(`/projects/${id}`, { method: "DELETE" }),

  // Documents
  listDocuments: (projectId: string) =>
    request<Document[]>(`/projects/${projectId}/documents/`),
  uploadDocument: (projectId: string, file: File, docType: string = "rfp") => {
    const formData = new FormData();
    formData.append("file", file);
    return request<Document>(`/projects/${projectId}/documents/upload?doc_type=${docType}`, {
      method: "POST",
      body: formData,
    });
  },
  deleteDocument: (projectId: string, docId: string) =>
    request<void>(`/projects/${projectId}/documents/${docId}`, { method: "DELETE" }),

  // Sections
  listSections: (projectId: string) =>
    request<Section[]>(`/projects/${projectId}/sections/`),
  getSection: (projectId: string, sectionId: string) =>
    request<Section>(`/projects/${projectId}/sections/${sectionId}`),
  updateSection: (projectId: string, sectionId: string, data: { content?: string; status?: string }) =>
    request<Section>(`/projects/${projectId}/sections/${sectionId}`, {
      method: "PATCH",
      body: JSON.stringify(data),
    }),
  generateSections: (projectId: string, tabKeys?: string[], additionalContext?: string) =>
    request<GenerateResponse>(`/projects/${projectId}/sections/generate`, {
      method: "POST",
      body: JSON.stringify({ tab_keys: tabKeys, additional_context: additionalContext }),
    }),
  regenerateSection: (projectId: string, tabKey: string, additionalContext?: string) =>
    request<Section>(`/projects/${projectId}/sections/${tabKey}/regenerate?additional_context=${additionalContext || ""}`, {
      method: "POST",
    }),

  // Export
  exportProject: async (projectId: string, format: string = "docx", tabKeys?: string[]) => {
    const url = `${API_BASE}/projects/${projectId}/export/`;
    const res = await fetch(url, {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({ format, tab_keys: tabKeys }),
    });
    if (!res.ok) throw new Error("Export failed");
    return res.blob();
  },
};

// Types
export interface Project {
  id: string;
  name: string;
  description: string | null;
  client_name: string | null;
  industry: string | null;
  submission_deadline: string | null;
  status: string;
  additional_context: string | null;
  created_at: string;
  updated_at: string;
}

export interface CreateProjectData {
  name: string;
  description?: string;
  client_name?: string;
  industry?: string;
  submission_deadline?: string;
  additional_context?: string;
}

export interface Document {
  id: string;
  project_id: string;
  filename: string;
  file_type: string;
  file_size: number | null;
  doc_type: string;
  created_at: string;
}

export interface Section {
  id: string;
  project_id: string;
  tab_key: string;
  tab_index: number;
  title: string;
  content: string | null;
  confidence_score: number | null;
  citations: string | null;
  version: number;
  status: string;
  created_at: string;
  updated_at: string;
}

export interface GenerateResponse {
  message: string;
  sections: Section[];
}
