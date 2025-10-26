import React, { useState, useEffect } from "react";
import axios from "axios";

const API_BASE = "http://localhost:8080/api/documents";

const DocumentManager = ({ onLogout }) => {
  const userId = localStorage.getItem("userId");
  const [file, setFile] = useState(null);
  const [documents, setDocuments] = useState([]);
  const [keyword, setKeyword] = useState("");
  const [fileSearchKeywords, setFileSearchKeywords] = useState({});
  const [fileSearchResults, setFileSearchResults] = useState({}); // store results per file

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

  // Global search documents
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
      setFileSearchResults((prev) => {
        const copy = { ...prev };
        delete copy[id];
        return copy;
      });
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

  // Per-file search
  const handleFileSearch = async (id) => {
    const keyword = fileSearchKeywords[id];
    if (!keyword || !keyword.trim()) return;

    try {
      const res = await axios.get(`${API_BASE}/${id}/search`, {
        params: { keyword },
      });
      setFileSearchResults((prev) => ({ ...prev, [id]: res.data }));
    } catch (err) {
      console.error(err);
      alert("Per-file search failed");
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

      {/* Global Search */}
      <form onSubmit={handleSearch}>
        <input
          type="text"
          placeholder="Search filename or text"
          value={keyword}
          onChange={(e) => setKeyword(e.target.value)}
        />
        <button type="submit">Search</button>
      </form>

      {/* Document Table */}
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
            <th>Search Inside File</th>
          </tr>
        </thead>
        <tbody>
          {documents.length === 0 && (
            <tr>
              <td colSpan="5" style={{ textAlign: "center" }}>
                No documents found
              </td>
            </tr>
          )}
          {documents.map((doc) => (
            <React.Fragment key={doc.id}>
              <tr>
                <td>{doc.filename}</td>
                <td>{doc.uploadedAtFormatted}</td>
                <td>
                  <button onClick={() => handleDownload(doc.id, doc.filename)}>Download</button>
                </td>
                <td>
                  <button onClick={() => handleDelete(doc.id)}>Delete</button>
                </td>
                <td>
                  <input
                    type="text"
                    placeholder="Keyword"
                    value={fileSearchKeywords[doc.id] || ""}
                    onChange={(e) =>
                      setFileSearchKeywords({ ...fileSearchKeywords, [doc.id]: e.target.value })
                    }
                  />
                  <button onClick={() => handleFileSearch(doc.id)}>Search</button>
                </td>
              </tr>

              {/* Highlighted Search Results */}
              {fileSearchResults[doc.id] && fileSearchResults[doc.id].length > 0 && (
                <tr>
                  <td colSpan="5" style={{ textAlign: "left", backgroundColor: "#f9f9f9" }}>
                    <strong>Search Results:</strong>
                    <ul>
                      {fileSearchResults[doc.id].map((line, index) => (
                        <li key={index}>
                          {line.split(new RegExp(`(${fileSearchKeywords[doc.id]})`, "gi")).map((part, i) =>
                            part.toLowerCase() === fileSearchKeywords[doc.id].toLowerCase() ? (
                              <mark key={i}>{part}</mark>
                            ) : (
                              part
                            )
                          )}
                        </li>
                      ))}
                    </ul>
                  </td>
                </tr>
              )}
            </React.Fragment>
          ))}
        </tbody>
      </table>
    </div>
  );
};

export default DocumentManager;
