'use client';
import { useEffect, useState } from 'react';
import Link from 'next/link';
import api from '@/lib/api';
import { addToCart } from '@/lib/cart';

interface Product {
  id: number;
  name: string;
  price: number;
  imageUrl: string;
  stockCount: number;
}

export default function HomePage() {
  const [products, setProducts] = useState<Product[]>([]);
  const [added, setAdded] = useState<number | null>(null);

  useEffect(() => {
    api.get('/api/products').then((r) => setProducts(r.data));
  }, []);

  const handleAdd = (product: Product) => {
    addToCart({ productId: product.id, productName: product.name, quantity: 1, unitPrice: product.price });
    setAdded(product.id);
    setTimeout(() => setAdded(null), 1500);
  };

  return (
    <main className="max-w-6xl mx-auto p-6">
      <div className="flex justify-between items-center mb-8">
        <h1 className="text-3xl font-bold">Products</h1>
        <div className="flex gap-4">
          <Link href="/cart" className="text-blue-600 hover:underline">Cart</Link>
          <Link href="/orders" className="text-blue-600 hover:underline">My Orders</Link>
          <Link href="/login" className="text-blue-600 hover:underline">Login</Link>
        </div>
      </div>
      <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-6">
        {products.map((product) => (
          <div key={product.id} className="border rounded-lg overflow-hidden shadow hover:shadow-md transition">
            {product.imageUrl && (
              // eslint-disable-next-line @next/next/no-img-element
              <img src={product.imageUrl} alt={product.name} className="w-full h-48 object-cover" />
            )}
            <div className="p-4">
              <h2 className="font-semibold text-lg">{product.name}</h2>
              <p className="text-gray-600">${Number(product.price).toFixed(2)}</p>
              <p className={`text-sm mt-1 ${product.stockCount > 0 ? 'text-green-600' : 'text-red-500'}`}>
                {product.stockCount > 0 ? `${product.stockCount} in stock` : 'Out of stock'}
              </p>
              <div className="mt-3 flex gap-2">
                <Link href={`/products/${product.id}`}
                  className="flex-1 text-center border border-blue-600 text-blue-600 rounded px-3 py-1 text-sm hover:bg-blue-50">
                  View
                </Link>
                <button
                  onClick={() => handleAdd(product)}
                  disabled={product.stockCount === 0}
                  className="flex-1 bg-blue-600 text-white rounded px-3 py-1 text-sm hover:bg-blue-700 disabled:opacity-50">
                  {added === product.id ? 'Added!' : 'Add to Cart'}
                </button>
              </div>
            </div>
          </div>
        ))}
      </div>
    </main>
  );
}
