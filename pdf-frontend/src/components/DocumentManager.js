import React, { useState, useEffect } from "react";
import axios from "axios";

const API_BASE = "http://localhost:8080/api/documents";

const DocumentManager = ({ onLogout }) => {
  const userId = localStorage.getItem("userId");
  const [file, setFile] = useState(null);
  const [documents, setDocuments] = useState([]);
  const [keyword, setKeyword] = useState("");

  // Fetch all documents
  const fetchDocuments = async () => {
    try {
      const res = await axios.get(API_BASE);
      setDocuments(res.data);
    } catch (err) {
      console.error(err);
      alert("Failed to fetch documents");
    }
  };

  useEffect(() => {
    fetchDocuments();
  }, []);

  // Upload PDF/DOC
  const handleUpload = async () => {
    if (!file) return alert("Select a file first!");
    const formData = new FormData();
    formData.append("file", file);
    formData.append("ownerId", userId);

    try {
      const res = await axios.post(`${API_BASE}/upload`, formData, {
        headers: { "Content-Type": "multipart/form-data" },
      });
      if (res.status === 200) {
        alert("File uploaded successfully!");
        setFile(null);
        fetchDocuments();
      }
    } catch (err) {
      console.error(err);
      alert("Upload failed");
    }
  };

  // Search documents
  const handleSearch = async (e) => {
    e.preventDefault();
    if (!keyword.trim()) return fetchDocuments();
    try {
      const res = await axios.get(`${API_BASE}/search`, {
        params: { keyword },
      });
      setDocuments(res.data);
    } catch (err) {
      console.error(err);
      alert("Search failed");
    }
  };

  // Delete document
  const handleDelete = async (id) => {
    if (!window.confirm("Are you sure to delete this file?")) return;
    try {
      await axios.delete(`${API_BASE}/${id}`);
      fetchDocuments();
    } catch (err) {
      console.error(err);
      alert("Delete failed");
    }
  };

  // Download extracted text
  const handleDownload = async (id, filename) => {
    try {
      const res = await axios.get(`${API_BASE}/${id}/download`, {
        responseType: "blob",
      });
      const url = window.URL.createObjectURL(new Blob([res.data]));
      const link = document.createElement("a");
      link.href = url;
      link.setAttribute("download", `${filename}.txt`);
      document.body.appendChild(link);
      link.click();
      link.remove();
    } catch (err) {
      console.error(err);
      alert("Download failed");
    }
  };

  return (
    <div style={{ textAlign: "center" }}>
      <div style={{ display: "flex", justifyContent: "space-between" }}>
        <h2>📄 Document Manager</h2>
        <button onClick={onLogout}>Logout</button>
      </div>

      {/* Upload */}
      <div style={{ margin: "15px 0" }}>
        <input type="file" onChange={(e) => setFile(e.target.files[0])} />
        <button onClick={handleUpload}>Upload PDF/DOC</button>
      </div>

      {/* Search */}
      <form onSubmit={handleSearch}>
        <input
          type="text"
          placeholder="Search filename or text"
          value={keyword}
          onChange={(e) => setKeyword(e.target.value)}
        />
        <button type="submit">Search</button>
      </form>

      {/* Table */}
      <table
        border="1"
        cellPadding="10"
        style={{ borderCollapse: "collapse", width: "100%", marginTop: "15px" }}
      >
        <thead>
          <tr>
            <th>Filename</th>
            <th>Uploaded At</th>
            <th>Download</th>
            <th>Delete</th>
          </tr>
        </thead>
        <tbody>
          {documents.length === 0 && (
            <tr>
              <td colSpan="4" style={{ textAlign: "center" }}>
                No documents found
              </td>
            </tr>
          )}
          {documents.map((doc) => (
            <tr key={doc.id}>
              <td>{doc.filename}</td>
              <td>{doc.uploadedAtFormatted}</td>
              <td>
                <button onClick={() => handleDownload(doc.id, doc.filename)}>
                  Download
                </button>
              </td>
              <td>
                <button onClick={() => handleDelete(doc.id)}>Delete</button>
              </td>
            </tr>
          ))}
        </tbody>
      </table>
    </div>
  );
};

export default DocumentManager;
