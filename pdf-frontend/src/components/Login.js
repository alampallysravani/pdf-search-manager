import React, { useState } from "react";
import axios from "axios";

const API_BASE = "http://localhost:8080/api/users";

function Login() {
  const [username, setUsername] = useState("");
  const [password, setPassword] = useState("");

  const handleLogin = async (e) => {
    e.preventDefault();
    try {
      const res = await axios.post(`${API_BASE}/login`, {
        username,
        password,
      });

      // ✅ Correct destructuring (NO duplicate variables)
      const { token, id, username: uname, email, role } = res.data;

localStorage.setItem("token", token);
localStorage.setItem("userId", id);
localStorage.setItem("username", uname);
localStorage.setItem("email", email);
localStorage.setItem("role", role);


if (role === "ADMIN") {
  window.location.href = "/admin";
} else {
  window.location.href = "/user";
}

    } catch (err) {
      console.error(err);
      alert("Login failed. Check username and password.");
    }
  };

  return (
  <div>
    <form onSubmit={handleLogin}>
      <h2>Login</h2>

      <input
        type="text"
        placeholder="Username"
        value={username}
        onChange={(e) => setUsername(e.target.value)}
        required
      />
      <br />

      <input
        type="password"
        placeholder="Password"
        value={password}
        onChange={(e) => setPassword(e.target.value)}
        required
      />
      <br />

      <button type="submit">Login</button>
    </form>

    <p>
      Don’t have an account?{" "}
      <a href="/register" style={{ color: "blue", cursor: "pointer" }}>
        Register here
      </a>
    </p>
  </div>
);

}

export default Login;
