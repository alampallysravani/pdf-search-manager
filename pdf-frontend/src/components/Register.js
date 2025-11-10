import React, { useState } from "react";
import axios from "axios";

const API_BASE = "http://localhost:8080/api/users";

function Register() {
  const [username, setUsername] = useState("");
  const [email, setEmail] = useState("");     // ✅ add email
  const [password, setPassword] = useState("");

  const register = async (e) => {
    e.preventDefault();
    try {
      await axios.post(`${API_BASE}/register`, {
        username,
        email,          // ✅ send email
        password
      });

      alert("Registered successfully!");
      window.location.href = "/login"; // ✅ redirect
    } catch (err) {
      console.error(err);
      alert("Registration failed");
    }
  };

  return (
    <form onSubmit={register}>
      <h2>Register</h2>

      <input
        type="text"
        placeholder="Username"
        value={username}
        onChange={(e) => setUsername(e.target.value)}
        required
      />
      <br />

      <input
        type="email"
        placeholder="Email"
        value={email}
        onChange={(e) => setEmail(e.target.value)}
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

      <button type="submit">Register</button>
    </form>
  );
}

export default Register;
