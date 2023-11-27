import './App.css';
import Navbar from './Components/Navbar';
import AboutUs from './Components/AboutUs';
import Home from './Components/Home';
import Devices from './Components/Devices';
import Rooms from './Components/Rooms';
import Profile from './Components/Profile';
import SignIn from './Components/SignIn';
import AddDevice from './Components/AddDevice';
import SignUp from './Components/SignUp';
import DetailDevice from './Components/DetailDevice';
import RoomDetails from './Components/RoomDetails';
import Dashboard from './Components/Dashboard';
import React from 'react';
import { AuthProvider } from './Context/AuthContext';
import { BrowserRouter as Router, Routes, Route } from 'react-router-dom';


function App() {
  return (
    <Router>
      <AuthProvider>
        <Navbar />
          <Routes>
            <Route path="/aboutus" element={<AboutUs />} />
            <Route path="/" element={<Home />} />
            <Route path="/devices" element={<Devices />} />
            <Route path="/rooms" element={<Rooms />} />
            <Route path="/profile" element={<Profile />} />
            <Route path="/signup" element={<SignUp />} />
            <Route path="/create_device" element={<AddDevice/>}/>
            <Route path="/detail_device/:id" element={<DetailDevice/>}/>
            <Route path="/signIn" element={<SignIn />} />
            <Route path="/roomdetails" element={<RoomDetails />} />
            <Route path="/reports" element={<Dashboard />} />
          </Routes>
        <Navbar />
      </AuthProvider>
    </Router>
  );
}

export default App;
