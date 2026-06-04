'use client';
import { useEffect, useState } from 'react';
import { useParams, useRouter } from 'next/navigation';
import Link from 'next/link';
import api from '@/lib/api';
import { addToCart } from '@/lib/cart';

interface Product {
  id: number;
  name: string;
  descriptionHtml: string;
  price: number;
  imageUrl: string;
  stockCount: number;
}

export default function ProductDetailPage() {
  const { id } = useParams();
  const router = useRouter();
  const [product, setProduct] = useState<Product | null>(null);
  const [qty, setQty] = useState(1);
  const [added, setAdded] = useState(false);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(false);
  const [stale, setStale] = useState(false);

  const fetchProduct = () => {
    setLoading(true);
    setError(false);
    setStale(false);
    api.get(`/api/products/${id}`)
      .then((r) => {
        setProduct(r.data);
        localStorage.setItem(`cached_product_${id}`, JSON.stringify(r.data));
      })
      .catch(() => {
        const cached = localStorage.getItem(`cached_product_${id}`);
        if (cached) {
          setProduct(JSON.parse(cached));
          setStale(true);
        } else {
          setError(true);
        }
      })
      .finally(() => setLoading(false));
  };

  useEffect(() => { fetchProduct(); }, [id]);

  if (loading) {
    return (
      <main className="max-w-5xl mx-auto px-6 py-10">
        <div className="animate-pulse">
          <div className="h-4 bg-surface rounded w-32 mb-8" />
          <div className="flex flex-col md:flex-row gap-10">
            <div className="w-full md:w-[420px] h-[320px] bg-surface rounded-xl" />
            <div className="flex-1 space-y-4">
              <div className="h-8 bg-surface rounded w-3/4" />
              <div className="h-6 bg-surface rounded w-1/4" />
              <div className="h-4 bg-surface rounded w-1/3" />
              <div className="h-12 bg-surface rounded w-48 mt-6" />
            </div>
          </div>
        </div>
      </main>
    );
  }

  if (error || !product) {
    return (
      <main className="max-w-5xl mx-auto px-6 py-10">
        <Link href="/" className="inline-flex items-center gap-1 text-muted hover:text-foreground text-sm font-medium mb-8 transition-colors">
          <svg className="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M15 19l-7-7 7-7" /></svg>
          Back to products
        </Link>
        <div className="text-center py-20 border border-dashed border-danger/30 rounded-xl bg-danger-light/30">
          <div className="text-5xl mb-4">⚠️</div>
          <h2 className="text-xl font-semibold text-foreground">Product unavailable</h2>
          <p className="text-muted mt-2">This product couldn&apos;t be loaded right now. Please try again shortly.</p>
          <button onClick={fetchProduct}
            className="mt-6 bg-primary text-white px-6 py-2.5 rounded-lg text-sm font-medium hover:bg-primary-hover transition-colors">
            Try Again
          </button>
        </div>
      </main>
    );
  }

  const handleAdd = () => {
    addToCart({ productId: product.id, productName: product.name, quantity: qty, unitPrice: product.price });
    setAdded(true);
    setTimeout(() => setAdded(false), 1500);
  };

  return (
    <main className="max-w-5xl mx-auto px-6 py-10">
      <Link href="/" className="inline-flex items-center gap-1 text-muted hover:text-foreground text-sm font-medium mb-8 transition-colors">
        <svg className="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M15 19l-7-7 7-7" /></svg>
        Back to products
      </Link>

      {stale && (
        <div className="mb-6 flex items-center gap-3 border border-warning/30 bg-warning-light/30 rounded-xl px-5 py-3">
          <span className="text-xl">📡</span>
          <div className="flex-1">
            <p className="text-sm font-medium text-foreground">Showing cached data</p>
            <p className="text-xs text-muted">Product service is unavailable — price and stock may be outdated.</p>
          </div>
          <button onClick={fetchProduct}
            className="text-xs font-medium text-primary hover:text-primary-hover transition-colors">
            Retry
          </button>
        </div>
      )}

      <div className="flex flex-col md:flex-row gap-10">
        {product.imageUrl ? (
          // eslint-disable-next-line @next/next/no-img-element
          <img src={product.imageUrl} alt={product.name}
            className="w-full md:w-[420px] h-[320px] object-cover rounded-xl border border-border" />
        ) : (
          <div className="w-full md:w-[420px] h-[320px] bg-surface rounded-xl border border-border flex items-center justify-center">
            <span className="text-6xl">📦</span>
          </div>
        )}

        <div className="flex-1">
          <h1 className="text-3xl font-bold tracking-tight">{product.name}</h1>
          <p className="text-3xl font-bold text-primary mt-3">${Number(product.price).toFixed(2)}</p>

          <div className="mt-4">
            <span className={`inline-flex items-center gap-1.5 text-sm font-medium px-3 py-1.5 rounded-full ${
              product.stockCount > 0
                ? 'bg-success-light text-success'
                : 'bg-danger-light text-danger'
            }`}>
              <span className={`w-1.5 h-1.5 rounded-full ${product.stockCount > 0 ? 'bg-success' : 'bg-danger'}`} />
              {product.stockCount > 0 ? `${product.stockCount} in stock` : 'Out of stock'}
            </span>
          </div>

          <div className="mt-8 flex items-center gap-4">
            <div className="flex items-center border border-border rounded-lg overflow-hidden">
              <button
                onClick={() => setQty(Math.max(1, qty - 1))}
                className="px-3 py-2.5 text-muted hover:text-foreground hover:bg-surface transition-colors">
                -
              </button>
              <input type="number" min={1} max={product.stockCount} value={qty}
                onChange={(e) => setQty(Math.max(1, Number(e.target.value)))}
                className="w-14 text-center border-x border-border py-2.5 text-sm font-medium focus:outline-none" />
              <button
                onClick={() => setQty(Math.min(product.stockCount, qty + 1))}
                className="px-3 py-2.5 text-muted hover:text-foreground hover:bg-surface transition-colors">
                +
              </button>
            </div>

            <button onClick={handleAdd} disabled={product.stockCount === 0}
              className={`px-8 py-2.5 rounded-lg text-sm font-medium transition-all duration-200 ${
                added
                  ? 'bg-success text-white'
                  : 'bg-primary text-white hover:bg-primary-hover'
              } disabled:opacity-40 disabled:cursor-not-allowed`}>
              {added ? 'Added to Cart!' : 'Add to Cart'}
            </button>
          </div>

          <button onClick={() => router.push('/cart')}
            className="mt-4 text-sm font-medium text-primary hover:text-primary-hover transition-colors inline-flex items-center gap-1">
            Go to Cart
            <svg className="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M9 5l7 7-7 7" /></svg>
          </button>
        </div>
      </div>

      {product.descriptionHtml && (
        <div className="mt-12 border-t border-border pt-8">
          <h2 className="text-xl font-semibold mb-4">Product Description</h2>
          <div
            className="prose max-w-none text-muted leading-relaxed"
            dangerouslySetInnerHTML={{ __html: product.descriptionHtml }}
          />
        </div>
      )}
    </main>
  );
}
