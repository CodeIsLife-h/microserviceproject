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

  // Confirmed state
  if (status === 'CONFIRMED') {
    return (
      <main className="min-h-[70vh] flex items-center justify-center px-6">
        <div className="text-center max-w-md">
          <div className="w-20 h-20 bg-success-light rounded-full flex items-center justify-center mx-auto mb-6">
            <svg className="w-10 h-10 text-success" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M5 13l4 4L19 7" />
            </svg>
          </div>
          <h1 className="text-2xl font-bold">Order Confirmed!</h1>
          <p className="text-muted mt-3">Order #{orderId} has been confirmed. A confirmation email has been sent to your inbox.</p>
          <button onClick={() => router.push('/orders')}
            className="mt-6 bg-primary text-white px-6 py-2.5 rounded-lg text-sm font-medium hover:bg-primary-hover transition-colors">
            View My Orders
          </button>
        </div>
      </main>
    );
  }

  // Failed state
  if (status === 'FAILED') {
    return (
      <main className="min-h-[70vh] flex items-center justify-center px-6">
        <div className="text-center max-w-md">
          <div className="w-20 h-20 bg-danger-light rounded-full flex items-center justify-center mx-auto mb-6">
            <svg className="w-10 h-10 text-danger" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M6 18L18 6M6 6l12 12" />
            </svg>
          </div>
          <h1 className="text-2xl font-bold">Order Failed</h1>
          <p className="text-muted mt-3">Insufficient stock for one or more items in your order. Please try again with different quantities.</p>
          <button onClick={() => router.push('/')}
            className="mt-6 bg-primary text-white px-6 py-2.5 rounded-lg text-sm font-medium hover:bg-primary-hover transition-colors">
            Back to Shopping
          </button>
        </div>
      </main>
    );
  }

  // Pending state
  if (status === 'PENDING') {
    return (
      <main className="min-h-[70vh] flex items-center justify-center px-6">
        <div className="text-center">
          <div className="relative w-16 h-16 mx-auto mb-6">
            <div className="absolute inset-0 rounded-full border-4 border-surface" />
            <div className="absolute inset-0 rounded-full border-4 border-primary border-t-transparent animate-spin" />
          </div>
          <h1 className="text-xl font-semibold">Processing Order #{orderId}</h1>
          <p className="text-muted text-sm mt-2">Checking stock availability...</p>
        </div>
      </main>
    );
  }

  // Order summary
  return (
    <main className="max-w-2xl mx-auto px-6 py-10">
      <h1 className="text-3xl font-bold tracking-tight mb-2">Checkout</h1>
      <p className="text-muted mb-8">Review your order before placing it.</p>

      {error && (
        <div className="mb-6 bg-danger-light border border-danger/20 text-danger rounded-lg px-4 py-3 text-sm font-medium">
          {error}
        </div>
      )}

      <div className="border border-border rounded-xl overflow-hidden divide-y divide-border mb-6">
        {cart.map((item) => (
          <div key={item.productId} className="flex justify-between items-center p-5">
            <div className="flex items-center gap-4">
              <div className="w-10 h-10 bg-surface rounded-lg flex items-center justify-center text-lg">📦</div>
              <div>
                <p className="font-medium">{item.productName}</p>
                <p className="text-sm text-muted">{item.quantity} &times; ${item.unitPrice.toFixed(2)}</p>
              </div>
            </div>
            <p className="font-semibold">${(item.quantity * item.unitPrice).toFixed(2)}</p>
          </div>
        ))}
      </div>

      <div className="border border-border rounded-xl p-5 bg-surface/50">
        <div className="flex justify-between items-center">
          <div>
            <p className="text-sm text-muted">Order Total</p>
            <p className="text-2xl font-bold mt-0.5">${total.toFixed(2)}</p>
          </div>
          <button onClick={handlePlaceOrder} disabled={cart.length === 0}
            className="bg-primary text-white rounded-lg px-8 py-3 font-medium hover:bg-primary-hover disabled:opacity-40 disabled:cursor-not-allowed transition-colors">
            Place Order
          </button>
        </div>
      </div>
    </main>
  );
}
