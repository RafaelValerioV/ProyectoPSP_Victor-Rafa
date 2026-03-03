import Header from './Header';

export default function Layout({ children }) {
    return (
        <div className="min-h-screen bg-gray-50">
            <Header />
            <main className="max-w-4xl mx-auto px-6 py-8">
                {children}
            </main>
        </div>
    );
}
