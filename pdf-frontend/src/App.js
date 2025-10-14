import React, { useState } from "react";
import Login from "./components/Login";
import Register from "./components/Register";
import DocumentManager from "./components/DocumentManager";

function App() {
  const [userId, setUserId] = useState(localStorage.getItem("userId"));
  const [showRegister, setShowRegister] = useState(false);

  const handleLoginSuccess = () => setUserId(localStorage.getItem("userId"));
  const handleLogout = () => {
    localStorage.removeItem("userId");
    setUserId(null);
  };

  return (
    <div style={{ padding: "20px", fontFamily: "Arial" }}>
      {!userId && !showRegister && (
        <>
          <Login onLoginSuccess={handleLoginSuccess} />
          <p>
            Don't have an account?{" "}
            <button onClick={() => setShowRegister(true)}>Register</button>
          </p>
        </>
      )}

      {!userId && showRegister && (
        <>
          <Register />
          <p>
            Already have an account?{" "}
            <button onClick={() => setShowRegister(false)}>Login</button>
          </p>
        </>
      )}

      {userId && <DocumentManager onLogout={handleLogout} />}
    </div>
  );
}

export default App;
