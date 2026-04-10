import React, { useState } from 'react';
import { X, CheckCircle, CreditCard, Loader2 } from 'lucide-react';

export default function CheckoutModal({ property, onClose }) {
  const [status, setStatus] = useState('idle'); // idle, loading, success

  const handlePayment = () => {
    setStatus('loading');
    setTimeout(() => {
      setStatus('success');
    }, 2000);
  };

  return (
    <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50 p-4">
      <div className="bg-white rounded-lg shadow-xl w-full max-w-md overflow-hidden relative">
        <button 
          onClick={onClose} 
          className="absolute top-4 right-4 text-gray-400 hover:text-gray-600"
          disabled={status === 'loading'}
        >
          <X className="w-6 h-6" />
        </button>

        <div className="p-6">
          {status === 'idle' && (
            <>
              <h2 className="text-2xl font-bold mb-4">Confirm Booking</h2>
              <div className="mb-6 p-4 bg-gray-50 rounded-md border border-gray-100">
                <h3 className="font-semibold text-lg">{property.name}</h3>
                <p className="text-gray-500 text-sm mb-2">{property.location}</p>
                <div className="flex justify-between items-center mt-4">
                  <span className="text-gray-600">Total Amount:</span>
                  <span className="text-xl font-bold">${property.price}</span>
                </div>
              </div>
              <button 
                onClick={handlePayment}
                className="w-full py-3 bg-blue-600 text-white rounded-md font-medium flex items-center justify-center hover:bg-blue-700 transition"
              >
                <CreditCard className="w-5 h-5 mr-2" /> Pay with PayPal
              </button>
            </>
          )}

          {status === 'loading' && (
            <div className="py-12 flex flex-col items-center justify-center space-y-4">
              <Loader2 className="w-12 h-12 text-blue-600 animate-spin" />
              <p className="text-gray-600 font-medium">Processing payment via PayPal...</p>
            </div>
          )}

          {status === 'success' && (
            <div className="py-8 flex flex-col items-center justify-center space-y-4 text-center">
              <div className="w-16 h-16 bg-green-100 rounded-full flex items-center justify-center mb-2">
                <CheckCircle className="w-10 h-10 text-green-600" />
              </div>
              <h3 className="text-2xl font-bold text-gray-900">Payment Successful!</h3>
              <p className="text-gray-600 mb-6">Your reservation is confirmed. The invoice (JasperReports) has been generated and sent to your email.</p>
              <button 
                onClick={onClose}
                className="w-full py-2 bg-gray-100 text-gray-800 rounded-md font-medium hover:bg-gray-200 transition"
              >
                Close Summary
              </button>
            </div>
          )}
        </div>
      </div>
    </div>
  );
}