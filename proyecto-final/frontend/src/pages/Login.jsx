import React, { useState } from 'react';
import { useAuth } from '../context/AuthContext';
import { Lock, User, Hotel } from 'lucide-react';

export default function Login() {
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const { login } = useAuth();

  const handleSubmit = (e) => {
    e.preventDefault();
    // Simulating login request based on role
    const role = email.includes('admin') ? 'ADMIN' : 'CLIENT';
    login('dummy_jwt_token_12345', role);
  };

  return (
    <div className="flex items-center justify-center min-h-screen bg-gray-50">
      <div className="p-8 bg-white rounded-lg shadow-xl w-96">
        <div className="flex justify-center mb-6">
          <Hotel className="w-12 h-12 text-blue-600" />
        </div>
        <h2 className="mb-6 text-2xl font-bold text-center text-gray-800">Sign In</h2>
        <form onSubmit={handleSubmit}>
          <div className="mb-4">
            <div className="flex items-center px-3 py-2 border rounded-md bg-gray-50">
              <User className="w-5 h-5 text-gray-400 mr-2" />
              <input 
                type="email" 
                value={email} 
                onChange={e => setEmail(e.target.value)} 
                required 
                placeholder="Email address" 
                className="w-full bg-transparent outline-none text-gray-700"
              />
            </div>
            <p className="text-xs text-gray-500 mt-1">Hint: Use 'admin' in email for admin access</p>
          </div>
          <div className="mb-6">
            <div className="flex items-center px-3 py-2 border rounded-md bg-gray-50">
              <Lock className="w-5 h-5 text-gray-400 mr-2" />
              <input 
                type="password" 
                value={password} 
                onChange={e => setPassword(e.target.value)} 
                required 
                placeholder="Password" 
                className="w-full bg-transparent outline-none text-gray-700" 
              />
            </div>
          </div>
          <button 
            type="submit" 
            className="w-full py-2.5 text-white font-medium bg-blue-600 rounded-md hover:bg-blue-700 transition duration-300"
          >
            Login
          </button>
        </form>
      </div>
    </div>
  );
}