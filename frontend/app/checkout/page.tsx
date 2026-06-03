'use client';
import { useEffect, useState } from 'react';
import { useRouter } from 'next/navigation';
import api from '@/lib/api';
import { getCart, clearCart, CartItem } from '@/lib/cart';
import { isAuthenticated } from '@/lib/auth';

type OrderStatus = 'PENDING' | 'CONFIRMED' | 'FAILED';

export default function CheckoutPage() {
  const router = useRouter();
  const [cart, setCart] = useState<CartItem[]>([]);
  const [orderId, setOrderId] = useState<number | null>(null);
  const [status, setStatus] = useState<OrderStatus | null>(null);
  const [error, setError] = useState('');

  useEffect(() => {
    if (!isAuthenticated()) router.push('/login');
    setCart(getCart());
  }, [router]);

  const total = cart.reduce((sum, i) => sum + i.unitPrice * i.quantity, 0);

  const handlePlaceOrder = async () => {
    setError('');
    try {
      const { data } = await api.post('/api/orders', {
        items: cart.map((i) => ({
          productId: i.productId,
          productName: i.productName,
          quantity: i.quantity,
          unitPrice: i.unitPrice,
        })),
      });
      setOrderId(data.id);
      setStatus('PENDING');
      clearCart();
      pollStatus(data.id);
    } catch {
      setError('Failed to place order. Please try again.');
    }
  };

  const pollStatus = (id: number) => {
    const interval = setInterval(async () => {
      try {
        const { data } = await api.get(`/api/orders/${id}`);
        if (data.status !== 'PENDING') {
          setStatus(data.status as OrderStatus);
          clearInterval(interval);
        }
      } catch {
        clearInterval(interval);
      }
    }, 2000);
  };

  if (status === 'CONFIRMED') {
    return (
      <main className="min-h-screen flex items-center justify-center">
        <div className="text-center">
          <div className="text-5xl mb-4">✓</div>
          <h1 className="text-2xl font-bold text-green-600">Order Confirmed!</h1>
          <p className="text-gray-600 mt-2">Order #{orderId} has been confirmed. Check your email for details.</p>
          <button onClick={() => router.push('/orders')} className="mt-4 text-blue-600 hover:underline">
            View My Orders →
          </button>
        </div>
      </main>
    );
  }

  if (status === 'FAILED') {
    return (
      <main className="min-h-screen flex items-center justify-center">
        <div className="text-center">
          <div className="text-5xl mb-4">✗</div>
          <h1 className="text-2xl font-bold text-red-600">Order Failed</h1>
          <p className="text-gray-600 mt-2">Insufficient stock for one or more items.</p>
          <button onClick={() => router.push('/')} className="mt-4 text-blue-600 hover:underline">
            Back to Shopping
          </button>
        </div>
      </main>
    );
  }

  if (status === 'PENDING') {
    return (
      <main className="min-h-screen flex items-center justify-center">
        <div className="text-center">
          <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-blue-600 mx-auto mb-4" />
          <h1 className="text-xl font-semibold">Processing Order #{orderId}...</h1>
          <p className="text-gray-500 text-sm mt-2">Checking stock availability...</p>
        </div>
      </main>
    );
  }

  return (
    <main className="max-w-2xl mx-auto p-6">
      <h1 className="text-2xl font-bold mb-6">Order Summary</h1>
      {error && <p className="text-red-500 mb-4 text-sm">{error}</p>}
      <div className="divide-y border rounded-lg overflow-hidden mb-6">
        {cart.map((item) => (
          <div key={item.productId} className="flex justify-between p-4">
            <div>
              <p className="font-medium">{item.productName}</p>
              <p className="text-sm text-gray-500">Qty: {item.quantity} × ${item.unitPrice.toFixed(2)}</p>
            </div>
            <p className="font-semibold">${(item.quantity * item.unitPrice).toFixed(2)}</p>
          </div>
        ))}
      </div>
      <div className="flex justify-between items-center">
        <p className="text-xl font-bold">Total: ${total.toFixed(2)}</p>
        <button onClick={handlePlaceOrder} disabled={cart.length === 0}
          className="bg-blue-600 text-white rounded px-6 py-2 hover:bg-blue-700 disabled:opacity-50 font-medium">
          Place Order
        </button>
      </div>
    </main>
  );
}
