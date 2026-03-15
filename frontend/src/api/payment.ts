import client from './client'

export const confirmPayment = (paymentKey: string, orderId: string, amount: number) =>
  client.post('/payments/confirm', { paymentKey, orderId, amount }).then((r) => r.data)
