import React, { useState, useEffect, useCallback } from "react";
import axios from "axios";

const API_BASE = "http://localhost:8080/api/documents";

function DocumentManager() {
  const userId = localStorage.getItem("userId");
  const role = (localStorage.getItem("role") || "USER").toUpperCase();

  const [file, setFile] = useState(null);
  const [documents, setDocuments] = useState([]);
  const [search, setSearch] = useState("");
  const [fileSearchKeywords, setFileSearchKeywords] = useState({});

  // ✅ Fetch all documents (Admin and User)
  const fetchDocuments = useCallback(async () => {
    try {
      const res = await axios.get(`${API_BASE}`, {
        headers: { Authorization: `Bearer ${localStorage.getItem("token")}` },
      });
      setDocuments(res.data);
    } catch (err) {
      console.error(err);
      alert("⚠️ Failed to fetch documents. Please check backend.");
    }
  }, []);

  useEffect(() => {
    fetchDocuments();
  }, [fetchDocuments]);

  // ✅ Upload file (Admin only)
  const handleUpload = async () => {
    if (!file) return alert("Please select a file first!");
    if (!userId) return alert("User ID not found.");

    const formData = new FormData();
    formData.append("file", file);
    formData.append("ownerId", userId);

    try {
      const res = await axios.post(`${API_BASE}/upload`, formData, {
        headers: {
          "Content-Type": "multipart/form-data",
          Authorization: `Bearer ${localStorage.getItem("token")}`,
        },
      });
      alert(`✅ Uploaded successfully: ${res.data.filename}`);
      setFile(null);
      fetchDocuments();
    } catch (err) {
      console.error(err);
      alert(`❌ Upload failed: ${err.response?.data || "Server error"}`);
    }
  };

  // ✅ Delete file (Admin only)
  const handleDelete = async (id) => {
    if (!window.confirm("Are you sure you want to delete this document?")) return;
    try {
      const res = await axios.delete(`${API_BASE}/${id}`, {
        headers: { Authorization: `Bearer ${localStorage.getItem("token")}` },
      });
      alert(res.data || "Deleted successfully.");
      fetchDocuments();
    } catch (err) {
      console.error(err);
      alert(`❌ Delete failed: ${err.response?.data || "Server error"}`);
    }
  };

  // ✅ Download extracted text
  const handleDownload = async (id, filename) => {
    try {
      const res = await axios.get(`${API_BASE}/${id}/download`, {
        responseType: "blob",
        headers: { Authorization: `Bearer ${localStorage.getItem("token")}` },
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
      alert("❌ Download failed");
    }
  };

  // ✅ Global search
  const handleGlobalSearch = async (e) => {
    e.preventDefault();
    if (search.trim() === "") return fetchDocuments();

    try {
      const res = await axios.get(`${API_BASE}/search`, {
        params: { keyword: search },
        headers: { Authorization: `Bearer ${localStorage.getItem("token")}` },
      });
      setDocuments(res.data);
    } catch (err) {
      console.error(err);
      alert("❌ Global search failed");
    }
  };

  // ✅ Search inside specific file and directly open it in new tab
  const handleInsideSearch = (id) => {
    const keyword = fileSearchKeywords[id];
    if (!keyword || keyword.trim() === "") {
      alert("Please enter a keyword to search.");
      return;
    }

    const fileUrl = `${API_BASE}/${id}/file#search=${encodeURIComponent(keyword)}`;
    window.open(fileUrl, "_blank"); // opens PDF directly with keyword highlighted
  };

  // ✅ Logout
  const handleLogout = () => {
    localStorage.clear();
    window.location.href = "/login";
  };

  return (
    <div style={{ padding: "20px", maxWidth: "1000px", margin: "auto" }}>
      <h2>📁 Document Manager ({role})</h2>

      {role === "ADMIN" && (
        <div style={{ margin: "15px 0" }}>
          <input
            type="file"
            onChange={(e) => setFile(e.target.files[0])}
            accept=".pdf,.docx"
          />
          <button onClick={handleUpload} style={{ marginLeft: "10px" }}>
            Upload PDF/DOCX
          </button>
        </div>
      )}

      <form onSubmit={handleGlobalSearch} style={{ marginBottom: "20px" }}>
        <input
          type="text"
          placeholder="🔍 Search all documents"
          value={search}
          onChange={(e) => setSearch(e.target.value)}
          style={{ width: "300px", padding: "6px" }}
        />
        <button type="submit" style={{ marginLeft: "10px" }}>
          Search
        </button>
      </form>

      <table
        border="1"
        cellPadding="10"
        style={{ width: "100%", borderCollapse: "collapse" }}
      >
        <thead>
          <tr style={{ backgroundColor: "#f4f4f4" }}>
            <th>Filename</th>
            <th>Uploaded Date</th>
            <th>Download</th>
            {role === "ADMIN" && <th>Delete</th>}
            <th>Search Keyword</th>
          </tr>
        </thead>
        <tbody>
          {documents.length === 0 ? (
            <tr>
              <td
                colSpan={role === "ADMIN" ? 5 : 4}
                style={{ textAlign: "center" }}
              >
                No documents found
              </td>
            </tr>
          ) : (
            documents.map((doc) => (
              <tr key={doc.id}>
                <td>
                  <a
                    href={`${API_BASE}/${doc.id}/file`}
                    target="_blank"
                    rel="noreferrer"
                    style={{ color: "blue", textDecoration: "underline" }}
                  >
                    {doc.filename}
                  </a>
                </td>
                <td>{doc.uploadedAt || "N/A"}</td>
                <td>
                  <button onClick={() => handleDownload(doc.id, doc.filename)}>
                    Download
                  </button>
                </td>
                {role === "ADMIN" && (
                  <td>
                    <button onClick={() => handleDelete(doc.id)}>Delete</button>
                  </td>
                )}
                <td>
                  <input
                    type="text"
                    placeholder="Keyword"
                    value={fileSearchKeywords[doc.id] || ""}
                    onChange={(e) =>
                      setFileSearchKeywords((prev) => ({
                        ...prev,
                        [doc.id]: e.target.value,
                      }))
                    }
                  />
                  <button
                    onClick={() => handleInsideSearch(doc.id)}
                    style={{ marginLeft: "5px" }}
                  >
                    Search
                  </button>
                </td>
              </tr>
            ))
          )}
        </tbody>
      </table>

      <button
        onClick={handleLogout}
        style={{
          marginTop: "20px",
          backgroundColor: "#d9534f",
          color: "white",
          border: "none",
          padding: "8px 12px",
          borderRadius: "5px",
        }}
      >
        Logout
      </button>
    </div>
  );
}

export default DocumentManager;
