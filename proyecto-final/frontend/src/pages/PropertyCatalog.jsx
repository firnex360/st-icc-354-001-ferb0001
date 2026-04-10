import React, { useState } from 'react';
import { useAuth } from '../context/AuthContext';
import { LogOut, Hotel, MapPin, Star } from 'lucide-react';
import CheckoutModal from '../components/CheckoutModal';

const properties = [
  { id: 1, name: 'Oceanview Villa', location: 'Miami, FL', price: 250, rating: 4.8, image: 'https://images.unsplash.com/photo-1499793983690-e29da59ef1c2?auto=format&fit=crop&q=80&w=400' },
  { id: 2, name: 'Mountain Retreat', location: 'Aspen, CO', price: 320, rating: 4.9, image: 'https://images.unsplash.com/photo-1510798831971-661eb04b3739?auto=format&fit=crop&q=80&w=400' },
  { id: 3, name: 'City Center Loft', location: 'New York, NY', price: 180, rating: 4.5, image: 'https://images.unsplash.com/photo-1502672260266-1c1de24244fe?auto=format&fit=crop&q=80&w=400' },
];

export default function PropertyCatalog() {
  const { logout } = useAuth();
  const [selectedProperty, setSelectedProperty] = useState(null);

  return (
    <div className="min-h-screen bg-gray-50">
      <header className="bg-white shadow-sm sticky top-0 z-10">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 h-16 flex justify-between items-center">
          <h1 className="text-2xl font-bold text-gray-900 flex items-center">
            <Hotel className="mr-2 text-blue-600" />
            Find Your Stay
          </h1>
          <button onClick={logout} className="flex items-center text-gray-500 hover:text-gray-900">
            <LogOut className="w-5 h-5 mr-1" /> Logout
          </button>
        </div>
      </header>

      <main className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-8">
          {properties.map(property => (
            <div key={property.id} className="bg-white rounded-lg shadow-md overflow-hidden hover:shadow-lg transition duration-300">
              <img src={property.image} alt={property.name} className="w-full h-48 object-cover" />
              <div className="p-5">
                <div className="flex justify-between items-start mb-2">
                  <h3 className="text-xl font-bold text-gray-900">{property.name}</h3>
                  <span className="flex items-center text-sm font-medium text-yellow-500">
                    <Star className="w-4 h-4 mr-1 fill-current" /> {property.rating}
                  </span>
                </div>
                <p className="text-gray-500 flex items-center text-sm mb-4">
                  <MapPin className="w-4 h-4 mr-1" /> {property.location}
                </p>
                <div className="flex justify-between items-center mt-4 pt-4 border-t border-gray-100">
                  <span className="text-2xl font-bold text-gray-900">${property.price}<span className="text-sm font-normal text-gray-500">/night</span></span>
                  <button 
                    onClick={() => setSelectedProperty(property)}
                    className="px-4 py-2 bg-blue-600 text-white text-sm font-medium rounded-md hover:bg-blue-700 transition"
                  >
                    Book Now
                  </button>
                </div>
              </div>
            </div>
          ))}
        </div>
      </main>

      {selectedProperty && (
        <CheckoutModal property={selectedProperty} onClose={() => setSelectedProperty(null)} />
      )}
    </div>
  );
}