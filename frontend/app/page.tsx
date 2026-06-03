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
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(false);

  const fetchProducts = () => {
    setLoading(true);
    setError(false);
    api.get('/api/products')
      .then((r) => setProducts(r.data))
      .catch(() => setError(true))
      .finally(() => setLoading(false));
  };

  useEffect(() => { fetchProducts(); }, []);

  const handleAdd = (product: Product) => {
    addToCart({ productId: product.id, productName: product.name, quantity: 1, unitPrice: product.price });
    setAdded(product.id);
    setTimeout(() => setAdded(null), 1500);
  };

  return (
    <main className="max-w-6xl mx-auto px-6 py-10">
      {/* Hero */}
      <div className="mb-10">
        <h1 className="text-4xl font-bold tracking-tight">Discover Products</h1>
        <p className="text-muted mt-2 text-lg">Browse our curated collection of quality items.</p>
      </div>

      {/* Loading state */}
      {loading && (
        <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-6">
          {[1, 2, 3].map((i) => (
            <div key={i} className="border border-border rounded-xl overflow-hidden animate-pulse">
              <div className="w-full h-52 bg-surface" />
              <div className="p-5 space-y-3">
                <div className="h-5 bg-surface rounded w-3/4" />
                <div className="h-4 bg-surface rounded w-1/4" />
                <div className="h-9 bg-surface rounded" />
              </div>
            </div>
          ))}
        </div>
      )}

      {/* Error state */}
      {!loading && error && (
        <div className="text-center py-20 border border-dashed border-danger/30 rounded-xl bg-danger-light/30">
          <div className="text-5xl mb-4">⚠️</div>
          <h2 className="text-xl font-semibold text-foreground">Products temporarily unavailable</h2>
          <p className="text-muted mt-2 max-w-md mx-auto">We&apos;re having trouble loading the product catalog. This is usually temporary.</p>
          <button onClick={fetchProducts}
            className="mt-6 bg-primary text-white px-6 py-2.5 rounded-lg text-sm font-medium hover:bg-primary-hover transition-colors">
            Try Again
          </button>
        </div>
      )}

      {/* Empty state */}
      {!loading && !error && products.length === 0 && (
        <div className="text-center py-20">
          <div className="text-6xl mb-4">🛍</div>
          <h2 className="text-xl font-semibold text-foreground">No products yet</h2>
          <p className="text-muted mt-2">Check back soon for new arrivals.</p>
        </div>
      )}

      {/* Product grid */}
      {!loading && !error && products.length > 0 && (
        <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-6">
          {products.map((product) => (
            <div key={product.id}
              className="group border border-border rounded-xl overflow-hidden bg-background hover:shadow-lg hover:border-primary/20 transition-all duration-200">
              <Link href={`/products/${product.id}`}>
                {product.imageUrl ? (
                  // eslint-disable-next-line @next/next/no-img-element
                  <img src={product.imageUrl} alt={product.name}
                    className="w-full h-52 object-cover group-hover:scale-[1.02] transition-transform duration-200" />
                ) : (
                  <div className="w-full h-52 bg-surface flex items-center justify-center">
                    <span className="text-4xl">📦</span>
                  </div>
                )}
              </Link>
              <div className="p-5">
                <Link href={`/products/${product.id}`}>
                  <h2 className="font-semibold text-lg group-hover:text-primary transition-colors">{product.name}</h2>
                </Link>
                <div className="flex items-center justify-between mt-2">
                  <p className="text-xl font-bold text-foreground">${Number(product.price).toFixed(2)}</p>
                  <span className={`text-xs font-medium px-2.5 py-1 rounded-full ${
                    product.stockCount > 0
                      ? 'bg-success-light text-success'
                      : 'bg-danger-light text-danger'
                  }`}>
                    {product.stockCount > 0 ? `${product.stockCount} in stock` : 'Out of stock'}
                  </span>
                </div>
                <div className="mt-4 flex gap-2">
                  <Link href={`/products/${product.id}`}
                    className="flex-1 text-center border border-border text-foreground rounded-lg px-3 py-2.5 text-sm font-medium hover:bg-surface transition-colors">
                    View Details
                  </Link>
                  <button
                    onClick={() => handleAdd(product)}
                    disabled={product.stockCount === 0}
                    className={`flex-1 rounded-lg px-3 py-2.5 text-sm font-medium transition-all duration-200 ${
                      added === product.id
                        ? 'bg-success text-white'
                        : 'bg-primary text-white hover:bg-primary-hover'
                    } disabled:opacity-40 disabled:cursor-not-allowed`}>
                    {added === product.id ? 'Added!' : 'Add to Cart'}
                  </button>
                </div>
              </div>
            </div>
          ))}
        </div>
      )}
    </main>
  );
}
