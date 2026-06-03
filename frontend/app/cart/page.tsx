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
    <main className="max-w-2xl mx-auto p-6">
      <div className="flex justify-between items-center mb-6">
        <h1 className="text-2xl font-bold">Your Cart</h1>
        <Link href="/" className="text-blue-600 hover:underline text-sm">← Continue Shopping</Link>
      </div>

      {cart.length === 0 ? (
        <p className="text-gray-500">Your cart is empty.</p>
      ) : (
        <>
          <div className="divide-y border rounded-lg overflow-hidden">
            {cart.map((item) => (
              <div key={item.productId} className="flex justify-between items-center p-4">
                <div>
                  <p className="font-medium">{item.productName}</p>
                  <p className="text-sm text-gray-500">Qty: {item.quantity} × ${item.unitPrice.toFixed(2)}</p>
                </div>
                <p className="font-semibold">${(item.quantity * item.unitPrice).toFixed(2)}</p>
              </div>
            ))}
          </div>
          <div className="mt-4 flex justify-between items-center">
            <p className="text-lg font-bold">Total: ${total.toFixed(2)}</p>
            <div className="flex gap-3">
              <button onClick={handleClear} className="text-red-500 hover:underline text-sm">Clear Cart</button>
              <Link href="/checkout"
                className="bg-blue-600 text-white rounded px-5 py-2 text-sm hover:bg-blue-700">
                Checkout →
              </Link>
            </div>
          </div>
        </>
      )}
    </main>
  );
}
