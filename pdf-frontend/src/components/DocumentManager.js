import React, { useState, useEffect } from "react";
import axios from "axios";

const API_BASE = "http://localhost:8080/api/documents";

const DocumentManager = ({ onLogout }) => {
  const userId = localStorage.getItem("userId");

  const [file, setFile] = useState(null);
  const [documents, setDocuments] = useState([]);
  const [keyword, setKeyword] = useState("");

  // ✅ Upload file
  const handleUpload = async () => {
    if (!file) {
      alert("Please select a file to upload");
      return;
    }

    const formData = new FormData();
    formData.append("file", file);
    formData.append("ownerId", userId);

    try {
      const response = await axios.post(`${API_BASE}/upload`, formData, {
        headers: { "Content-Type": "multipart/form-data" },
      });

      if (response.status === 200) {
        alert("✅ File uploaded successfully!");
        setFile(null);
        fetchDocuments(); // refresh list
      } else {
        alert("❌ Upload failed. Server responded with status " + response.status);
      }
    } catch (err) {
      console.error("Upload failed:", err);
      alert("Upload failed. Check backend connection or logs.");
    }
  };

  // ✅ Fetch all documents
  const fetchDocuments = async () => {
    try {
      const res = await axios.get(API_BASE);
      setDocuments(res.data);
    } catch (err) {
      console.error("Error fetching documents:", err);
      alert("Failed to fetch documents. Check backend logs.");
    }
  };

  // ✅ Search by file content (via backend)
  const handleSearch = async () => {
    if (!keyword) {
      fetchDocuments();
      return;
    }

    try {
      const res = await axios.get(`${API_BASE}/search`, {
        params: { keyword },
      });
      setDocuments(res.data);
    } catch (err) {
      console.error("Search failed:", err);
      alert("Search failed. Check backend logs.");
    }
  };

  // ✅ Fetch all documents on load
  useEffect(() => {
    fetchDocuments();
  }, []);

  // ✅ Logout function
  const handleLogout = () => {
    localStorage.removeItem("userId");
    if (onLogout) onLogout();
  };

  // ✅ Format date to DD-MM-YYYY HH:mm
  const formatDate = (dateStr) => {
    const d = new Date(dateStr);
    const day = String(d.getDate()).padStart(2, "0");
    const month = String(d.getMonth() + 1).padStart(2, "0");
    const year = d.getFullYear();
    const hours = String(d.getHours()).padStart(2, "0");
    const minutes = String(d.getMinutes()).padStart(2, "0");
    return `${day}-${month}-${year} ${hours}:${minutes}`;
  };

  return (
    <div style={{ padding: "20px", fontFamily: "Arial", textAlign: "center" }}>
      {/* Header */}
      <div style={{ display: "flex", justifyContent: "space-between", alignItems: "center" }}>
        <h2>📄 PDF / DOC Manager</h2>
        <button
          onClick={handleLogout}
          style={{
            backgroundColor: "#333",
            color: "white",
            border: "none",
            padding: "6px 12px",
            borderRadius: "5px",
            cursor: "pointer",
          }}
        >
          Logout
        </button>
      </div>

      {/* Upload Section */}
      <div style={{ marginBottom: "15px", marginTop: "10px" }}>
        <input type="file" onChange={(e) => setFile(e.target.files[0])} />
        <button
          onClick={handleUpload}
          style={{
            marginLeft: "8px",
            backgroundColor: "green",
            color: "white",
            border: "none",
            padding: "6px 12px",
            borderRadius: "5px",
            cursor: "pointer",
          }}
        >
          Upload
        </button>
      </div>

      {/* Search Section */}
      <div style={{ marginBottom: "20px" }}>
        <input
          type="text"
          placeholder="🔍 Search by content or filename"
          value={keyword}
          onChange={(e) => setKeyword(e.target.value)}
        />
        <button
          onClick={handleSearch}
          style={{
            marginLeft: "8px",
            backgroundColor: "#007bff",
            color: "white",
            border: "none",
            padding: "6px 12px",
            borderRadius: "5px",
            cursor: "pointer",
          }}
        >
          Search
        </button>
      </div>

      {/* Documents List */}
      <h3>Uploaded Documents</h3>
      {documents.length === 0 ? (
        <p>No documents found.</p>
      ) : (
        <ul style={{ listStyle: "none", padding: 0 }}>
          {documents.map((doc) => (
            <li key={doc.id} style={{ marginBottom: "10px" }}>
              <strong>{doc.filename}</strong> - <em>{formatDate(doc.uploadedAt)}</em>{" "}
              <a
                href={`${API_BASE}/${doc.id}/download`}
                target="_blank"
                rel="noopener noreferrer"
                style={{ color: "blue", marginLeft: "8px" }}
              >
                Download
              </a>
              <button
                style={{
                  marginLeft: "10px",
                  backgroundColor: "red",
                  color: "white",
                  border: "none",
                  padding: "5px 10px",
                  borderRadius: "5px",
                  cursor: "pointer",
                }}
                onClick={async () => {
                  if (window.confirm(`Delete ${doc.filename}?`)) {
                    try {
                      await axios.delete(`${API_BASE}/${doc.id}`);
                      fetchDocuments(); // refresh list
                    } catch (err) {
                      console.error("Error deleting document", err);
                      alert("Failed to delete file.");
                    }
                  }
                }}
              >
                Delete
              </button>
            </li>
          ))}
        </ul>
      )}
    </div>
  );
};

export default DocumentManager;
