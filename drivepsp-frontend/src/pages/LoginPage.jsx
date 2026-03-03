import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import toast from 'react-hot-toast';
import { useAuth } from '../context/AuthContext';

export default function LoginPage() {
    const [isRegister, setIsRegister] = useState(false);
    const [email, setEmail] = useState('');
    const [password, setPassword] = useState('');
    const [name, setName] = useState('');
    const [loading, setLoading] = useState(false);

    const { login, register } = useAuth();
    const navigate = useNavigate();

    async function handleSubmit(e) {
        e.preventDefault();
        setLoading(true);

        try {
            if (isRegister) {
                await register(email, password, name);
            } else {
                await login(email, password);
            }
            navigate('/', { replace: true });
        } catch (err) {
            toast.error(err.response?.data?.error || 'Error de autenticacion');
        } finally {
            setLoading(false);
        }
    }

    return (
        <div className="min-h-screen bg-gray-50 flex items-center justify-center px-4">
            <div className="w-full max-w-sm bg-white rounded-xl shadow-sm border border-gray-200 p-8">
                <img src="/logo.png" alt="DrivePSP" className="h-16 mx-auto mb-4" />
                <p className="text-sm text-center text-gray-500 mb-6">
                    {isRegister ? 'Crea tu cuenta' : 'Inicia sesion'}
                </p>

                <form onSubmit={handleSubmit} className="flex flex-col gap-4">
                    {isRegister && (
                        <input
                            type="text"
                            placeholder="Nombre"
                            value={name}
                            onChange={(e) => setName(e.target.value)}
                            required
                            className="border border-gray-300 rounded-lg px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-blue-500"
                        />
                    )}
                    <input
                        type="email"
                        placeholder="Email"
                        value={email}
                        onChange={(e) => setEmail(e.target.value)}
                        required
                        className="border border-gray-300 rounded-lg px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-blue-500"
                    />
                    <input
                        type="password"
                        placeholder="Contraseña"
                        value={password}
                        onChange={(e) => setPassword(e.target.value)}
                        required
                        minLength={6}
                        className="border border-gray-300 rounded-lg px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-blue-500"
                    />
                    <button
                        type="submit"
                        disabled={loading}
                        className="bg-blue-600 text-white py-2 rounded-lg hover:bg-blue-700 disabled:opacity-50 text-sm font-medium cursor-pointer"
                    >
                        {loading ? 'Cargando...' : isRegister ? 'Registrarse' : 'Entrar'}
                    </button>
                </form>

                <p className="text-center text-sm text-gray-500 mt-4">
                    {isRegister ? '¿Ya tienes cuenta?' : '¿No tienes cuenta?'}{' '}
                    <button
                        onClick={() => setIsRegister(!isRegister)}
                        className="text-blue-600 hover:underline cursor-pointer"
                    >
                        {isRegister ? 'Inicia sesion' : 'Registrate'}
                    </button>
                </p>

                <p className="text-center text-xs text-gray-400 mt-6">
                    Hecho para la asignatura PSP por Victor Lacruz y Rafael Valerio
                </p>
            </div>
        </div>
    );
}
