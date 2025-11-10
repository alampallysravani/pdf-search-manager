import React, { useEffect } from "react";
import DocumentManager from "./DocumentManager";

function AdminDashboard() {
  const username = localStorage.getItem("username") || "Admin";
  const role = (localStorage.getItem("role") || "").toUpperCase();

  // ✅ Block non-admin access
  useEffect(() => {
    if (role !== "ADMIN") {
      alert("⚠️ Unauthorized access! Redirecting to login...");
      localStorage.clear();
      window.location.href = "/login";
    }
  }, [role]);

  // ✅ Logout function
  const handleLogout = () => {
    localStorage.clear();
    window.location.href = "/login";
  };

  return (
    <div style={styles.container}>
      <header style={styles.header}>
        <div style={styles.headerLeft}>
          <h2>👨‍💼 Admin Dashboard</h2>
          <p>Welcome, <strong>{username}</strong></p>
        </div>
        <button onClick={handleLogout} style={styles.logoutButton}>
          Logout
        </button>
      </header>

      <main style={styles.main}>
        {/* ✅ DocumentManager handles upload/delete/search */}
        <DocumentManager />
      </main>
    </div>
  );
}

// ✅ Inline styling for quick integration
const styles = {
  container: {
    minHeight: "100vh",
    backgroundColor: "#f9fafb",
    color: "#333",
    padding: "20px",
    fontFamily: "Arial, sans-serif",
  },
  header: {
    display: "flex",
    justifyContent: "space-between",
    alignItems: "center",
    backgroundColor: "#004aad",
    color: "#fff",
    padding: "10px 20px",
    borderRadius: "8px",
  },
  headerLeft: {
    textAlign: "left",
  },
  logoutButton: {
    backgroundColor: "#ff4d4d",
    border: "none",
    color: "#fff",
    padding: "8px 16px",
    borderRadius: "6px",
    cursor: "pointer",
    fontSize: "14px",
  },
  main: {
    marginTop: "20px",
  },
};

export default AdminDashboard;
