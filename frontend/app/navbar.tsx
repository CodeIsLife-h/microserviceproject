'use client';
import { useEffect, useState } from 'react';
import Link from 'next/link';
import { usePathname } from 'next/navigation';
import { isAuthenticated, clearToken } from '@/lib/auth';
import { getCart } from '@/lib/cart';

export default function Navbar() {
  const pathname = usePathname();
  const [authed, setAuthed] = useState(false);
  const [cartCount, setCartCount] = useState(0);

  useEffect(() => {
    setAuthed(isAuthenticated());
    setCartCount(getCart().reduce((sum, i) => sum + i.quantity, 0));
  }, [pathname]);

  const handleLogout = () => {
    clearToken();
    setAuthed(false);
    window.location.href = '/';
  };

  const linkClass = (path: string) =>
    `text-sm font-medium transition-colors ${
      pathname === path
        ? 'text-primary'
        : 'text-muted hover:text-foreground'
    }`;

  return (
    <header className="sticky top-0 z-50 bg-background/80 backdrop-blur-md border-b border-border">
      <nav className="max-w-6xl mx-auto px-6 h-16 flex items-center justify-between">
        <Link href="/" className="flex items-center gap-2">
          <div className="w-8 h-8 bg-primary rounded-lg flex items-center justify-center">
            <span className="text-white font-bold text-sm">S</span>
          </div>
          <span className="font-semibold text-lg tracking-tight">ShopFlow</span>
        </Link>

        <div className="flex items-center gap-6">
          <Link href="/" className={linkClass('/')}>Products</Link>
          <Link href="/orders" className={linkClass('/orders')}>Orders</Link>
          <Link href="/cart" className={`${linkClass('/cart')} relative`}>
            Cart
            {cartCount > 0 && (
              <span className="absolute -top-2 -right-4 bg-primary text-white text-[10px] font-bold rounded-full w-5 h-5 flex items-center justify-center">
                {cartCount}
              </span>
            )}
          </Link>

          <div className="w-px h-6 bg-border" />

          {authed ? (
            <button onClick={handleLogout} className="text-sm font-medium text-muted hover:text-foreground transition-colors">
              Logout
            </button>
          ) : (
            <Link href="/login"
              className="text-sm font-medium bg-primary text-white px-4 py-2 rounded-lg hover:bg-primary-hover transition-colors">
              Sign In
            </Link>
          )}
        </div>
      </nav>
    </header>
  );
}
