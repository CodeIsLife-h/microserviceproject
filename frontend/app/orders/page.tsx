'use client';
import { useEffect, useState } from 'react';
import { useRouter } from 'next/navigation';
import Link from 'next/link';
import api from '@/lib/api';
import { isAuthenticated } from '@/lib/auth';

interface Order {
  id: number;
  status: string;
  total: number;
  createdAt: string;
  items: { productName: string; quantity: number; unitPrice: number }[];
}

const statusConfig: Record<string, { bg: string; text: string; dot: string }> = {
  PENDING: { bg: 'bg-warning-light', text: 'text-warning', dot: 'bg-warning' },
  CONFIRMED: { bg: 'bg-success-light', text: 'text-success', dot: 'bg-success' },
  FAILED: { bg: 'bg-danger-light', text: 'text-danger', dot: 'bg-danger' },
};

export default function OrdersPage() {
  const router = useRouter();
  const [orders, setOrders] = useState<Order[]>([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    if (!isAuthenticated()) { router.push('/login'); return; }
    api.get('/api/orders/my')
      .then((r) => setOrders(r.data))
      .finally(() => setLoading(false));
  }, [router]);

  if (loading) {
    return (
      <main className="max-w-3xl mx-auto px-6 py-10">
        <div className="h-8 bg-surface rounded w-40 mb-8 animate-pulse" />
        <div className="space-y-4">
          {[1, 2].map((i) => (
            <div key={i} className="border border-border rounded-xl p-5 animate-pulse">
              <div className="flex justify-between">
                <div className="space-y-2">
                  <div className="h-5 bg-surface rounded w-28" />
                  <div className="h-4 bg-surface rounded w-40" />
                </div>
                <div className="h-6 bg-surface rounded-full w-24" />
              </div>
            </div>
          ))}
        </div>
      </main>
    );
  }

  return (
    <main className="max-w-3xl mx-auto px-6 py-10">
      <div className="flex justify-between items-center mb-8">
        <div>
          <h1 className="text-3xl font-bold tracking-tight">My Orders</h1>
          <p className="text-muted mt-1">{orders.length} {orders.length === 1 ? 'order' : 'orders'}</p>
        </div>
        <Link href="/"
          className="inline-flex items-center gap-1 text-sm font-medium text-primary hover:text-primary-hover transition-colors">
          Continue Shopping
          <svg className="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M9 5l7 7-7 7" /></svg>
        </Link>
      </div>

      {orders.length === 0 ? (
        <div className="text-center py-20 border border-dashed border-border rounded-xl">
          <div className="text-5xl mb-4">📋</div>
          <h2 className="text-lg font-semibold">No orders yet</h2>
          <p className="text-muted mt-1">Your order history will appear here.</p>
          <Link href="/"
            className="inline-block mt-6 bg-primary text-white px-6 py-2.5 rounded-lg text-sm font-medium hover:bg-primary-hover transition-colors">
            Start Shopping
          </Link>
        </div>
      ) : (
        <div className="space-y-4">
          {orders.map((order) => {
            const sc = statusConfig[order.status] || statusConfig.PENDING;
            return (
              <div key={order.id} className="border border-border rounded-xl overflow-hidden hover:border-primary/20 transition-colors">
                <div className="flex justify-between items-start p-5 bg-surface/30">
                  <div>
                    <p className="font-semibold text-lg">Order #{order.id}</p>
                    <p className="text-sm text-muted mt-0.5">
                      {new Date(order.createdAt).toLocaleDateString('en-US', {
                        year: 'numeric', month: 'long', day: 'numeric', hour: '2-digit', minute: '2-digit',
                      })}
                    </p>
                  </div>
                  <span className={`inline-flex items-center gap-1.5 text-xs font-semibold px-3 py-1.5 rounded-full ${sc.bg} ${sc.text}`}>
                    <span className={`w-1.5 h-1.5 rounded-full ${sc.dot}`} />
                    {order.status}
                  </span>
                </div>
                <div className="divide-y divide-border">
                  {order.items.map((item, i) => (
                    <div key={i} className="flex justify-between items-center px-5 py-3 text-sm">
                      <div className="flex items-center gap-3">
                        <span className="text-muted">{item.quantity}&times;</span>
                        <span className="font-medium">{item.productName}</span>
                      </div>
                      <span className="text-muted">${(item.quantity * item.unitPrice).toFixed(2)}</span>
                    </div>
                  ))}
                </div>
                <div className="px-5 py-3 bg-surface/30 flex justify-end">
                  <p className="font-bold">Total: ${Number(order.total).toFixed(2)}</p>
                </div>
              </div>
            );
          })}
        </div>
      )}
    </main>
  );
}
