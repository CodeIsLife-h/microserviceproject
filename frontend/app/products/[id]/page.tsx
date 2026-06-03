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

  useEffect(() => {
    api.get(`/api/products/${id}`).then((r) => setProduct(r.data));
  }, [id]);

  if (!product) return <p className="p-6">Loading...</p>;

  const handleAdd = () => {
    addToCart({ productId: product.id, productName: product.name, quantity: qty, unitPrice: product.price });
    setAdded(true);
    setTimeout(() => setAdded(false), 1500);
  };

  return (
    <main className="max-w-4xl mx-auto p-6">
      <Link href="/" className="text-blue-600 hover:underline text-sm mb-4 block">← Back to products</Link>
      <div className="flex flex-col md:flex-row gap-8">
        {product.imageUrl && (
          // eslint-disable-next-line @next/next/no-img-element
          <img src={product.imageUrl} alt={product.name} className="w-full md:w-80 h-64 object-cover rounded-lg" />
        )}
        <div className="flex-1">
          <h1 className="text-2xl font-bold">{product.name}</h1>
          <p className="text-2xl text-blue-600 font-semibold mt-2">${Number(product.price).toFixed(2)}</p>
          <p className={`text-sm mt-1 ${product.stockCount > 0 ? 'text-green-600' : 'text-red-500'}`}>
            {product.stockCount > 0 ? `${product.stockCount} in stock` : 'Out of stock'}
          </p>
          <div className="mt-4 flex items-center gap-3">
            <label className="text-sm font-medium">Qty:</label>
            <input type="number" min={1} max={product.stockCount} value={qty}
              onChange={(e) => setQty(Number(e.target.value))}
              className="w-16 border rounded px-2 py-1 text-sm" />
            <button onClick={handleAdd} disabled={product.stockCount === 0}
              className="bg-blue-600 text-white rounded px-4 py-2 text-sm hover:bg-blue-700 disabled:opacity-50">
              {added ? 'Added!' : 'Add to Cart'}
            </button>
          </div>
          <button onClick={() => router.push('/cart')} className="mt-3 text-blue-600 hover:underline text-sm">
            Go to Cart →
          </button>
        </div>
      </div>

      {product.descriptionHtml && (
        <div className="mt-8 border-t pt-6">
          <h2 className="text-lg font-semibold mb-3">Product Description</h2>
          <div
            className="prose max-w-none"
            dangerouslySetInnerHTML={{ __html: product.descriptionHtml }}
          />
        </div>
      )}
    </main>
  );
}
