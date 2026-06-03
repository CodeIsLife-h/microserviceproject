export interface CartItem {
  productId: number;
  productName: string;
  quantity: number;
  unitPrice: number;
}

const CART_KEY = 'cart';

export const getCart = (): CartItem[] => {
  if (typeof window === 'undefined') return [];
  const raw = localStorage.getItem(CART_KEY);
  return raw ? JSON.parse(raw) : [];
};

export const addToCart = (item: CartItem) => {
  const cart = getCart();
  const existing = cart.find((i) => i.productId === item.productId);
  if (existing) {
    existing.quantity += item.quantity;
  } else {
    cart.push(item);
  }
  localStorage.setItem(CART_KEY, JSON.stringify(cart));
};

export const clearCart = () => localStorage.removeItem(CART_KEY);
