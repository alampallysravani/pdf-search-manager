import axios from "axios";

// Base URL for all document-related endpoints
const API_BASE = "http://localhost:8080/api/documents";

// ✅ Upload a document
export const uploadDocument = (file, ownerId) => {
  const formData = new FormData();
  formData.append("file", file);
  formData.append("ownerId", ownerId); // 👈 matches backend

  return axios.post(`${API_BASE}/upload`, formData, {
    headers: { "Content-Type": "multipart/form-data" },
  });
};

// ✅ Get all documents
export const getAllDocuments = () => {
  return axios.get(API_BASE);
};

// ✅ Download a document by ID
export const downloadDocument = (id) => {
  return axios({
    url: `${API_BASE}/${id}/download`,
    method: "GET",
    responseType: "blob", // important for file data
  }).then((res) => {
    const url = window.URL.createObjectURL(new Blob([res.data]));
    const link = document.createElement("a");
    const contentDisposition = res.headers["content-disposition"];
    let fileName = "file.pdf";
    if (contentDisposition) {
      const match = contentDisposition.match(/filename="?(.+)"?/);
      if (match && match[1]) fileName = match[1];
    }
    link.href = url;
    link.setAttribute("download", fileName);
    document.body.appendChild(link);
    link.click();
    link.remove();
  });
};

// ✅ Default axios export (so Login/Register can use it directly)
export default axios;
