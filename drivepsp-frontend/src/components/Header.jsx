import { LogOut } from 'lucide-react';
import { useAuth } from '../context/AuthContext';
import StorageBar from './StorageBar';

export default function Header() {
    const { user, logout } = useAuth();

    return (
        <header className="bg-white border-b border-gray-200 px-4 py-3">
            <div className="mx-auto flex items-center justify-between">
                <div className="flex items-center gap-3">
                    {user && <StorageBar label="Servidor" used={user.serverStorageUsed} limit={user.serverStorageLimit} />}
                    <h1 className="text-xl font-bold text-gray-900">DrivePSP</h1>
                </div>
                <div className="flex items-center gap-3">
                    {user && <StorageBar label="Mi espacio" used={user.storageUsed} limit={user.storageLimit} />}
                    {user && <span className="text-sm text-gray-600">{user.name}</span>}
                    <button
                        onClick={logout}
                        className="flex items-center gap-1 text-sm text-gray-500 hover:text-gray-700 cursor-pointer"
                    >
                        <LogOut size={16} />
                        Salir
                    </button>
                </div>
            </div>
        </header>
    );
}
