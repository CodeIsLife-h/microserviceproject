'use client';
import { useEffect, useState } from 'react';
import Link from 'next/link';
import { getCart, CartItem, clearCart } from '@/lib/cart';

export default function CartPage() {
  const [cart, setCart] = useState<CartItem[]>([]);

  useEffect(() => {
    setCart(getCart());
  }, []);

  const total = cart.reduce((sum, item) => sum + item.unitPrice * item.quantity, 0);

  const handleClear = () => {
    clearCart();
    setCart([]);
  };

  return (
    <main className="max-w-3xl mx-auto px-6 py-10">
      <div className="flex justify-between items-center mb-8">
        <div>
          <h1 className="text-3xl font-bold tracking-tight">Your Cart</h1>
          <p className="text-muted mt-1">{cart.length} {cart.length === 1 ? 'item' : 'items'}</p>
        </div>
        <Link href="/" className="inline-flex items-center gap-1 text-sm font-medium text-muted hover:text-foreground transition-colors">
          <svg className="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M15 19l-7-7 7-7" /></svg>
          Continue Shopping
        </Link>
      </div>

      {cart.length === 0 ? (
        <div className="text-center py-20 border border-dashed border-border rounded-xl">
          <div className="text-5xl mb-4">🛒</div>
          <h2 className="text-lg font-semibold">Your cart is empty</h2>
          <p className="text-muted mt-1">Add some products to get started.</p>
          <Link href="/"
            className="inline-block mt-6 bg-primary text-white px-6 py-2.5 rounded-lg text-sm font-medium hover:bg-primary-hover transition-colors">
            Browse Products
          </Link>
        </div>
      ) : (
        <>
          <div className="border border-border rounded-xl overflow-hidden divide-y divide-border">
            {cart.map((item) => (
              <div key={item.productId} className="flex justify-between items-center p-5 hover:bg-surface/50 transition-colors">
                <div className="flex items-center gap-4">
                  <div className="w-12 h-12 bg-surface rounded-lg flex items-center justify-center text-xl">📦</div>
                  <div>
                    <p className="font-medium">{item.productName}</p>
                    <p className="text-sm text-muted mt-0.5">
                      {item.quantity} &times; ${item.unitPrice.toFixed(2)}
                    </p>
                  </div>
                </div>
                <p className="font-semibold text-lg">${(item.quantity * item.unitPrice).toFixed(2)}</p>
              </div>
            ))}
          </div>

          <div className="mt-6 border border-border rounded-xl p-5 bg-surface/50">
            <div className="flex justify-between items-center">
              <div>
                <p className="text-sm text-muted">Subtotal</p>
                <p className="text-2xl font-bold mt-0.5">${total.toFixed(2)}</p>
              </div>
              <div className="flex items-center gap-3">
                <button onClick={handleClear}
                  className="text-sm font-medium text-danger hover:text-danger/80 transition-colors px-4 py-2.5 rounded-lg border border-danger/20 hover:bg-danger-light">
                  Clear Cart
                </button>
                <Link href="/checkout"
                  className="bg-primary text-white rounded-lg px-6 py-2.5 text-sm font-medium hover:bg-primary-hover transition-colors inline-flex items-center gap-1">
                  Checkout
                  <svg className="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M9 5l7 7-7 7" /></svg>
                </Link>
              </div>
            </div>
          </div>
        </>
      )}
    </main>
  );
}
