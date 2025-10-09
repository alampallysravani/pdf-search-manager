import React, { useState } from "react";
import Login from "./components/Login";
import Register from "./components/Register";
import DocumentManager from "./components/DocumentManager";

function App() {
  const [isLoggedIn, setIsLoggedIn] = useState(false);
  const [showLogin, setShowLogin] = useState(true);

  return (
    <div style={{ textAlign: "center", marginTop: "50px" }}>
      {!isLoggedIn ? (
        <div>
          <h1>Welcome to PDF Manager</h1>
          <div style={{ marginBottom: "20px" }}>
            <button onClick={() => setShowLogin(true)}>Login</button>
            <button onClick={() => setShowLogin(false)}>Register</button>
          </div>

          {showLogin ? (
            <Login onLoginSuccess={() => setIsLoggedIn(true)} />
          ) : (
            <Register />
          )}
        </div>
      ) : (
        <DocumentManager />
      )}
    </div>
  );
}

export default App;
