import { createContext, useContext, useState, useEffect } from 'react';
import * as authApi from '../api/auth';

const AuthContext = createContext(null);

export function AuthProvider({ children }) {
    const [token, setToken] = useState(localStorage.getItem('token'));
    const [user, setUser] = useState(null);
    const [loading, setLoading] = useState(true);

    // Cargar datos del usuario cuando hay token
    useEffect(() => {
        if (token) {
            refreshUser().finally(() => setLoading(false));
        } else {
            setLoading(false);
        }
    }, []);

    async function refreshUser() {
        try {
            const res = await authApi.me();
            setUser(res.data);
        } catch {
            logout();
        }
    }

    async function login(email, password) {
        const res = await authApi.login(email, password);
        localStorage.setItem('token', res.data.token);
        setToken(res.data.token);
        const meRes = await authApi.me();
        setUser(meRes.data);
    }

    async function register(email, password, name) {
        const res = await authApi.register(email, password, name);
        localStorage.setItem('token', res.data.token);
        setToken(res.data.token);
        const meRes = await authApi.me();
        setUser(meRes.data);
    }

    function logout() {
        localStorage.removeItem('token');
        setToken(null);
        setUser(null);
    }

    return (
        <AuthContext.Provider value={{ token, user, loading, login, register, logout, refreshUser }}>
            {children}
        </AuthContext.Provider>
    );
}

export function useAuth() {
    const context = useContext(AuthContext);
    if (!context) {
        throw new Error('useAuth debe usarse dentro de AuthProvider');
    }
    return context;
}
