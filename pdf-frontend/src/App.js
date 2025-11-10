import React from "react";
import { Routes, Route, Navigate } from "react-router-dom";

import Login from "./components/Login";
import Register from "./components/Register";
import DocumentManager from "./components/DocumentManager";
import AdminDashboard from "./components/AdminDashboard";

function App() {
  const role = (localStorage.getItem("role") || "").toUpperCase();

  return (
    <Routes>
      <Route path="/login" element={<Login />} />
      <Route path="/register" element={<Register />} />

      <Route
        path="/admin"
        element={
          role === "ADMIN" ? <AdminDashboard /> : <Navigate to="/login" />
        }
      />

      <Route
        path="/user"
        element={
          role === "USER" ? <DocumentManager /> : <Navigate to="/login" />
        }
      />

      <Route path="*" element={<Navigate to="/login" />} />
    </Routes>
  );
}

export default App;
