import { useRef, useState } from 'react';
import { Upload } from 'lucide-react';
import toast from 'react-hot-toast';
import * as filesApi from '../api/files';
import { useAuth } from '../context/AuthContext';

export default function UploadButton({ onUploaded }) {
    const inputRef = useRef(null);
    const [uploading, setUploading] = useState(false);
    const { refreshUser } = useAuth();

    async function handleFile(e) {
        const file = e.target.files[0];
        if (!file) return;

        setUploading(true);
        try {
            await filesApi.upload(file);
            await refreshUser();
            onUploaded();
            toast.success('Archivo subido');
        } catch (err) {
            const status = err.response?.status;
            if (status === 507) {
                toast.error('No hay espacio suficiente');
            } else {
                toast.error(err.response?.data?.error || 'Error al subir el archivo');
            }
        } finally {
            setUploading(false);
            inputRef.current.value = '';
        }
    }

    return (
        <>
            <input ref={inputRef} type="file" onChange={handleFile} className="hidden" />
            <button
                onClick={() => inputRef.current.click()}
                disabled={uploading}
                className="flex items-center gap-2 bg-blue-600 text-white px-4 py-2 rounded-lg hover:bg-blue-700 disabled:opacity-50 cursor-pointer"
            >
                <Upload size={18} />
                {uploading ? 'Subiendo...' : 'Subir archivo'}
            </button>
        </>
    );
}
