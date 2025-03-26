import React, { useState } from "react";
import { BarChart, Bar, XAxis, YAxis, CartesianGrid, Tooltip, Legend, ResponsiveContainer, PieChart, Pie, Cell } from "recharts";
import { Bell, Bot, MessageCircle } from "lucide-react";
import {toast, ToastContainer} from 'react-toastify';
import 'react-toastify/dist/ReactToastify.css';

export default function App() {
  const [isAnimating, setIsAnimating] = useState(false);
  const [uploadedFile, setUploadedFile] = useState(null);
  const [isChatOpen, setIsChatOpen] = useState(false);
  const [gridData, setGridData] = useState([
    { file: "file1.xlsx", anomalies: 10, comments: "", ticketId: "#1234", status: "Open", anomalyStatus: "Pending", outputFile: "output1.xlsx", isEditing: true },
    { file: "file2.xlsx", anomalies: 7, comments: "", ticketId: "#1235", status: "Closed", anomalyStatus: "Resolved", outputFile: "output2.xlsx", isEditing: true },
  ]);
  const styles = {
  botAnimation: {
    animation: 'moveBot 1s infinite'
  },
  '@keyframes moveBot': {
    '0%, 100%': { transform: 'translateX(0)' },
    '50%': { transform: 'translateX(20px)' }
  }
};

  const [showNotifications, setShowNotifications] = useState(false);
  const [showProfileOptions, setShowProfileOptions] = useState(false);
  const handleFileChange = (e) => {
    const file = e.target.files[0];
    if (file && (file.name.endsWith('.xls') || file.name.endsWith('.xlsx'))) {
      setUploadedFile(file);
    } else {
      toast.error("Please upload a valid Excel file (.xls or .xlsx)");
      e.target.value = ""; // Reset the input
    }
  };
  const handleGoSaraClick = () => {
    if (!uploadedFile) {
      toast.error("Please upload a transaction details Excel file before triggering Go SARA!");
      return;
    }
	toast.success("Go SARA Triggered!!");
    setIsAnimating(true);
    setTimeout(() => {
      setIsAnimating(false);
      toast.success("SARA finished its job. Over to my friend Reconciler!!");
    }, 20000); // Stop after 20 seconds
}
  const notifications = [
    "Anomaly #1234 resolved",
    "New anomaly detected: High-Frequency",
    "Ticket #1235 closed successfully"
  ];

  const anomalyBarData = [
    { month: "January", anomalies: 12 },
    { month: "February", anomalies: 8 },
    { month: "March", anomalies: 15 },
    { month: "April", anomalies: 10 },
    { month: "May", anomalies: 7 },
    { month: "June", anomalies: 11 },
    { month: "July", anomalies: 9 },
    { month: "August", anomalies: 14 },
    { month: "September", anomalies: 6 },
    { month: "October", anomalies: 13 },
    { month: "November", anomalies: 8 },
    { month: "December", anomalies: 10 },
  ];

  const handleCommentAction = (index) => {
    const updatedData = [...gridData];
    updatedData[index].isEditing = !updatedData[index].isEditing;
    setGridData(updatedData);
  };

  const handleCommentChange = (index, value) => {
    const updatedData = [...gridData];
    updatedData[index].comments = value;
    setGridData(updatedData);
  };

  const anomalyData = [
    { name: "Rapid Fund Movement", value: 10 },
    { name: "Location Based", value: 5 },
    { name: "High-Frequency", value: 7 },
    { name: "Others", value: 3 },
  ];

  const COLORS = ["#0088FE", "#00C49F", "#FFBB28", "#FF8042"];
  const [isLoggedIn, setIsLoggedIn] = useState(false);
  const [username, setUsername] = useState("");
  const [password, setPassword] = useState("");

  const handleLogin = () => {
    if (username === "user" && password === "password") {
      setIsLoggedIn(true);
    } else {
      alert("Invalid credentials");
    }
  };

  const handleLogout = () => {
    setIsLoggedIn(false);
    setUsername("");
    setPassword("");
  };

  if (!isLoggedIn) {
    return (
      <div className="h-screen flex items-center justify-center bg-gray-100">
        <div className="bg-white p-6 rounded-lg shadow-lg w-96">
          <h2 className="text-2xl font-bold mb-4">Login</h2>
          <input
            type="text"
            placeholder="Username"
            value={username}
            onChange={(e) => setUsername(e.target.value)}
            className="border p-2 rounded-lg w-full mb-4"
          />
          <input
            type="password"
            placeholder="Password"
            value={password}
            onChange={(e) => setPassword(e.target.value)}
            className="border p-2 rounded-lg w-full mb-4"
          />
          <button
            onClick={handleLogin}
            className="bg-blue-500 text-white p-2 rounded-lg w-full"
          >
            Login
          </button>
        </div>
      </div>
    );
  }

  return (
    <div className="flex h-screen">
      <style>{`
        @keyframes moveBot {
          0%, 100% { transform: translateX(0); }
          50% { transform: translateX(20px); }
        }
        .bot-animation {
          animation: moveBot 1s infinite;
        }
      `}</style>
      {/* Main Content */}
      <div className="flex-1 p-6 bg-gray-100 overflow-auto">
        <ToastContainer/>
        {/* Header with User Profile and Notifications */}
        <div className="flex justify-between items-center mb-6">
          <h1 className="text-2xl font-bold text-center flex-1">Smart Anomaly detection & Reconciliation Agent</h1>
          <div className="bg-white p-2 rounded-lg shadow-md flex items-center space-x-2 relative">
            <Bell
              className="w-6 h-6 text-blue-500 cursor-pointer"
              onClick={() => setShowNotifications(!showNotifications)}
            />
            {showNotifications && (
              <div className="absolute top-10 right-0 bg-white shadow-lg rounded-lg w-64 p-2">
                {notifications.map((notification, index) => (
                  <div key={index} className="text-sm border-b last:border-0 p-2">
                    {notification}
                  </div>
                ))}
              </div>
            )}
            <div className="relative">
            <div
                className="w-8 h-8 bg-blue-500 rounded-full flex items-center justify-center text-white cursor-pointer"
                onClick={() => setShowProfileOptions(!showProfileOptions)}
              >
              U
            </div>
            {showProfileOptions && (
                <div className="absolute top-10 right-0 bg-white shadow-lg rounded-lg w-32 p-2">
                  <button
                    onClick={handleLogout}
                    className="text-red-500 hover:text-blue-500 w-full text-left"
                  >
                    Logout
                  </button>
                </div>
              )}
            </div>
            <span className="text-sm font-medium">User</span>
          </div>
        </div>

        {/* Upload Section and Pie Chart */}
        <div className="flex space-x-6 mb-6">
          {/* Upload Button */}
          <div className="bg-white p-4 rounded-lg shadow-md flex-1">
            <h3 className="text-lg font-bold mb-4">Upload Transaction Details</h3>
            <input
              type="file"
              accept=".xls,.xlsx"
              className="border border-gray-300 p-2 rounded-lg w-full mb-4"
              onChange={handleFileChange}
            />
            <div className="flex justify-center mt-2 space-x-4">
              <div>
              <Bot
                className={`text-red-500 hover:text-blue-500 ${isAnimating ? 'bot-animation' : ''}`}
                onClick={handleGoSaraClick}
                size={64}
                label="GO SARA"
                />
              <span className="text-sm font-bold text-red-500">GO SARA</span>
                </div>
            </div>
          </div>

          {/* Pie Chart */}
          <div className="bg-white p-4 rounded-lg shadow-md w-1/3">
            <h3 className="text-lg font-bold mb-4">Anomaly Distribution</h3>
            <ResponsiveContainer width="100%" height={200}>
              <PieChart>
                <Pie
                  data={anomalyData}
                  cx="50%"
                  cy="50%"
                  outerRadius={60}
                  fill="#8884d8"
                  dataKey="value"
                  label
                >
                  {anomalyData.map((entry, index) => (
                    <Cell key={`cell-${index}`} fill={COLORS[index % COLORS.length]} />
                  ))}
                </Pie>
                <Tooltip />
                <Legend verticalAlign="bottom" height={36} />
              </PieChart>
            </ResponsiveContainer>
          </div>
        </div>

        {/* Grid */}
        <div className="bg-white p-4 rounded-lg shadow-md mb-6">
          <h3 className="text-lg font-bold mb-4">Transaction Anomalies</h3>
          <table className="w-full text-left border-collapse">
            <thead>
              <tr>
                <th>File</th>
                <th>Anomalies</th>
                <th>Comments</th>
                <th>Ticket ID</th>
                <th>Ticket Status</th>
                <th>Anomaly Status</th>
                <th>Output File</th>
                <th>Action</th>
              </tr>
            </thead>
            <tbody>
              {gridData.map((item, index) => (
                <tr key={index} className="border-t">
                  <td>{item.file}</td>
                  <td>{item.anomalies}</td>
                  <td>
                    {item.isEditing ? (
                      <input
                        value={item.comments}
                        onChange={(e) => handleCommentChange(index, e.target.value)}
                        className="border p-1"
                      />
                    ) : (
                      item.comments
                    )}
                  </td>
                  <td>{item.ticketId}</td>
                  <td>{item.status}</td>
                  <td>{item.anomalyStatus}</td>
                  <td>{item.outputFile}</td>
                  <td>
                    <button
                      onClick={() => handleCommentAction(index)}
                      className="text-blue-500 hover:underline"
                    >
                      {item.isEditing ? "Add Comments" : "Edit Comments"}
                    </button>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
         {/* Bar Graph */}
        <div className="bg-white p-4 rounded-lg shadow-md mt-6">
          <h3 className="text-lg font-bold mb-4">Anomaly Trends</h3>
          <ResponsiveContainer width="100%" height={300}>
            <BarChart data={anomalyBarData} margin={{ top: 5, right: 30, left: 20, bottom: 5 }}>
              <CartesianGrid strokeDasharray="3 3" />
              <XAxis dataKey="month" />
              <YAxis />
              <Tooltip />
              <Legend />
              <Bar dataKey="anomalies" fill="#8884d8" />
            </BarChart>
          </ResponsiveContainer>
        </div>
      </div>
      {/* Chatbot */}
      <div className="fixed bottom-4 right-4">
        <button onClick={() => setIsChatOpen(!isChatOpen)} className="bg-blue-500 text-white p-3 rounded-full shadow-lg hover:bg-blue-600 focus:outline-none">
          <MessageCircle size={24} />
        </button>
        {isChatOpen && (
          <div className="bg-white shadow-lg rounded-lg p-4 w-64 h-80 fixed bottom-16 right-4 flex flex-col">
            <h3 className="text-lg font-bold mb-2">Chatbot</h3>
            <div className="flex-1 overflow-auto border border-gray-200 p-2 mb-2 rounded-lg">Hello! How can I assist you?</div>
            <input type="text" placeholder="Type a message..." className="border p-2 rounded-lg w-full" />
          </div>
        )}
      </div>
    </div>
  );
}
