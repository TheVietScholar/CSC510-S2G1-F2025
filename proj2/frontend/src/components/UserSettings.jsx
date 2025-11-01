import React, { useEffect, useState } from 'react'

const UserSettings = ({ onBack, asModal = false, onClose }) => {
  const [address, setAddress] = useState({ line1: '', line2: '', city: '', state: '', zip: '' })
  const [payment, setPayment] = useState({ cardName: '', cardNumber: '', exp: '', cvc: '' })
  const [saved, setSaved] = useState(false)

  useEffect(() => {
    try {
      const a = JSON.parse(localStorage.getItem('bb_address') || 'null')
      const p = JSON.parse(localStorage.getItem('bb_payment') || 'null')
      if (a) setAddress(a)
      if (p) setPayment(p)
    } catch {}
  }, [])

  const saveAll = () => {
    localStorage.setItem('bb_address', JSON.stringify(address))
    localStorage.setItem('bb_payment', JSON.stringify(payment))
    setSaved(true)
    setTimeout(() => setSaved(false), 1500)
  }

  if (asModal) {
    return (
      <div className="p-6">
        <div className="flex items-center justify-between mb-4">
          <h1 className="text-2xl font-bold">User Settings</h1>
          <button onClick={onClose} className="px-3 py-1 rounded-lg border border-gray-300 bg-white hover:bg-gray-100 transition">Close</button>
        </div>

        <div className="space-y-8">
          <section>
            <h2 className="text-lg font-semibold mb-3">Address</h2>
            <div className="grid grid-cols-1 gap-3">
              <input className="border border-gray-300 rounded-lg px-3 py-2" placeholder="Address line 1" value={address.line1} onChange={e => setAddress({ ...address, line1: e.target.value })} />
              <input className="border border-gray-300 rounded-lg px-3 py-2" placeholder="Address line 2" value={address.line2} onChange={e => setAddress({ ...address, line2: e.target.value })} />
              <input className="border border-gray-300 rounded-lg px-3 py-2" placeholder="City" value={address.city} onChange={e => setAddress({ ...address, city: e.target.value })} />
              <input className="border border-gray-300 rounded-lg px-3 py-2" placeholder="State" value={address.state} onChange={e => setAddress({ ...address, state: e.target.value })} />
              <input className="border border-gray-300 rounded-lg px-3 py-2" placeholder="ZIP" value={address.zip} onChange={e => setAddress({ ...address, zip: e.target.value })} />
            </div>
          </section>

          <section>
            <h2 className="text-lg font-semibold mb-3">Payment (Frontend only)</h2>
            <div className="grid grid-cols-1 gap-3">
              <input className="border border-gray-300 rounded-lg px-3 py-2" placeholder="Name on card" value={payment.cardName} onChange={e => setPayment({ ...payment, cardName: e.target.value })} />
              <div className="grid grid-cols-3 gap-3">
                <input className="border border-gray-300 rounded-lg px-3 py-2 col-span-2" placeholder="Card number" value={payment.cardNumber} onChange={e => setPayment({ ...payment, cardNumber: e.target.value })} />
                <input className="border border-gray-300 rounded-lg px-3 py-2" placeholder="CVC" value={payment.cvc} onChange={e => setPayment({ ...payment, cvc: e.target.value })} />
              </div>
              <input className="border border-gray-300 rounded-lg px-3 py-2" placeholder="MM/YY" value={payment.exp} onChange={e => setPayment({ ...payment, exp: e.target.value })} />
              <p className="mt-1 text-xs text-gray-500">Data is stored locally for demo purposes. TODO: add server-side schema and endpoints.</p>
            </div>
          </section>

          <div className="flex justify-end">
            <button onClick={saveAll} className="px-4 py-2 rounded-lg bg-red-600 text-white hover:bg-red-700 transition">Save</button>
          </div>
          {saved && <div className="text-green-600 text-sm">Saved.</div>}
        </div>
      </div>
    )
  }

  return (
    <div className="min-h-screen bg-gray-50 text-gray-900">
      <div style={{ position: 'fixed', top: 0, left: 0, right: 0, zIndex: 50, background: '#f9fafb', borderBottom: '1px solid #e5e7eb' }}>
        <div className="max-w-3xl mx-auto px-6 py-3 flex items-center justify-between">
          <button onClick={onBack} className="px-4 py-2 rounded-lg border border-gray-300 bg-white hover:bg-gray-100 transition">← Back</button>
          <h1 className="text-lg font-semibold">User Settings</h1>
          <div className="w-[90px]" />
        </div>
      </div>

      <div style={{ height: 72 }} />

      <div className="max-w-3xl mx-auto px-6 pb-10 grid grid-cols-1 gap-8">
        <div className="bg-white p-6 rounded-xl border border-transparent shadow-sm">
          <h2 className="text-xl font-semibold mb-4">Address</h2>
          <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
            <input className="border border-gray-300 rounded-lg px-3 py-2" placeholder="Address line 1" value={address.line1} onChange={e => setAddress({ ...address, line1: e.target.value })} />
            <input className="border border-gray-300 rounded-lg px-3 py-2" placeholder="Address line 2" value={address.line2} onChange={e => setAddress({ ...address, line2: e.target.value })} />
            <input className="border border-gray-300 rounded-lg px-3 py-2" placeholder="City" value={address.city} onChange={e => setAddress({ ...address, city: e.target.value })} />
            <input className="border border-gray-300 rounded-lg px-3 py-2" placeholder="State" value={address.state} onChange={e => setAddress({ ...address, state: e.target.value })} />
            <input className="border border-gray-300 rounded-lg px-3 py-2" placeholder="ZIP" value={address.zip} onChange={e => setAddress({ ...address, zip: e.target.value })} />
          </div>
        </div>

        <div className="bg-white p-6 rounded-xl border border-transparent shadow-sm">
          <h2 className="text-xl font-semibold mb-4">Payment (Frontend only)</h2>
          <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
            <input className="border border-gray-300 rounded-lg px-3 py-2" placeholder="Name on card" value={payment.cardName} onChange={e => setPayment({ ...payment, cardName: e.target.value })} />
            <input className="border border-gray-300 rounded-lg px-3 py-2" placeholder="Card number" value={payment.cardNumber} onChange={e => setPayment({ ...payment, cardNumber: e.target.value })} />
            <input className="border border-gray-300 rounded-lg px-3 py-2" placeholder="MM/YY" value={payment.exp} onChange={e => setPayment({ ...payment, exp: e.target.value })} />
            <input className="border border-gray-300 rounded-lg px-3 py-2" placeholder="CVC" value={payment.cvc} onChange={e => setPayment({ ...payment, cvc: e.target.value })} />
          </div>
          <p className="mt-2 text-xs text-gray-500">Data is stored locally for demo purposes. TODO: add server-side schema and endpoints.</p>
        </div>

        <div className="flex justify-end">
          <button onClick={saveAll} className="px-4 py-2 rounded-lg bg-red-600 text-white hover:bg-red-700 transition">Save</button>
        </div>
        {saved && <div className="text-green-600 text-sm">Saved.</div>}
      </div>
    </div>
  )
}

export default UserSettings


