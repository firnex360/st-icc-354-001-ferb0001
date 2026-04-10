import React from 'react';
import { useAuth } from '../context/AuthContext';
import { BarChart, Bar, XAxis, YAxis, Tooltip, ResponsiveContainer, PieChart, Pie, Cell } from 'recharts';
import { ChartBar, LogOut, CheckCircle, Clock, XCircle } from 'lucide-react';

const barData = [
  { name: 'Pending', count: 12 },
  { name: 'Completed', count: 45 },
  { name: 'Cancelled', count: 5 },
];

const COLORS = ['#f59e0b', '#10b981', '#ef4444'];

export default function AdminDashboard() {
  const { logout } = useAuth();

  return (
    <div className="min-h-screen bg-gray-100 p-8">
      <div className="max-w-7xl mx-auto">
        <header className="flex justify-between items-center mb-8 bg-white p-6 rounded-lg shadow-sm">
          <h1 className="text-3xl font-bold text-gray-800 flex items-center">
            <ChartBar className="mr-3 text-blue-600" /> 
            Admin Dashboard
          </h1>
          <button 
            onClick={logout} 
            className="flex items-center px-4 py-2 text-sm font-medium text-red-600 bg-red-50 rounded-md hover:bg-red-100"
          >
            <LogOut className="mr-2 w-4 h-4" /> 
            Logout
          </button>
        </header>

        <div className="grid grid-cols-1 md:grid-cols-2 gap-6 mb-8">
          <div className="bg-white p-6 rounded-lg shadow-sm border border-gray-100 flex items-center">
            <div className="p-4 bg-blue-50 rounded-full mr-4">
              <Clock className="w-8 h-8 text-blue-600" />
            </div>
            <div>
              <h3 className="text-base text-gray-500 font-medium">Reservations Today</h3>
              <p className="text-3xl font-bold text-gray-900">24</p>
            </div>
          </div>
          <div className="bg-white p-6 rounded-lg shadow-sm border border-gray-100 flex items-center">
            <div className="p-4 bg-green-50 rounded-full mr-4">
              <CheckCircle className="w-8 h-8 text-green-600" />
            </div>
            <div>
              <h3 className="text-base text-gray-500 font-medium">Daily Revenue</h3>
              <p className="text-3xl font-bold text-gray-900">$3,450</p>
            </div>
          </div>
        </div>

        <div className="bg-white p-6 rounded-lg shadow-sm border border-gray-100">
          <h3 className="text-xl font-semibold mb-6 text-gray-800 border-b pb-4">Reservation Statuses</h3>
          <div className="h-80">
            <ResponsiveContainer width="100%" height="100%">
              <BarChart data={barData} margin={{ top: 20, right: 30, left: 20, bottom: 5 }}>
                <XAxis dataKey="name" />
                <YAxis />
                <Tooltip cursor={{fill: '#f3f4f6'}} />
                <Bar dataKey="count" radius={[4, 4, 0, 0]}>
                  {barData.map((entry, index) => (
                    <Cell key={`cell-${index}`} fill={COLORS[index % COLORS.length]} />
                  ))}
                </Bar>
              </BarChart>
            </ResponsiveContainer>
          </div>
        </div>
      </div>
    </div>
  );
}