import { useState } from 'react';
import { Download, Trash2, FileIcon } from 'lucide-react';
import toast from 'react-hot-toast';
import * as filesApi from '../api/files';
import { useAuth } from '../context/AuthContext';

export default function FileItem({ file, onDeleted }) {
    const [deleting, setDeleting] = useState(false);
    const [downloading, setDownloading] = useState(false);
    const { refreshUser } = useAuth();

    function formatSize(bytes) {
        if (bytes < 1024) return bytes + ' B';
        if (bytes < 1024 * 1024) return (bytes / 1024).toFixed(1) + ' KB';
        return (bytes / (1024 * 1024)).toFixed(1) + ' MB';
    }

    function formatDate(dateStr) {
        return new Date(dateStr).toLocaleDateString('es-ES', {
            day: '2-digit',
            month: '2-digit',
            year: 'numeric',
            hour: '2-digit',
            minute: '2-digit',
        });
    }

    async function handleDownload() {
        setDownloading(true);
        try {
            await filesApi.download(file.id, file.name);
        } catch {
            toast.error('Error al descargar el archivo');
        } finally {
            setDownloading(false);
        }
    }

    async function handleDelete() {
        setDeleting(true);
        try {
            await filesApi.remove(file.id);
            await refreshUser();
            onDeleted();
            toast.success('Archivo eliminado');
        } catch {
            toast.error('Error al eliminar el archivo');
        } finally {
            setDeleting(false);
        }
    }

    return (
        <div className="flex items-center justify-between py-3 px-4 bg-white rounded-lg border border-gray-200">
            <div className="flex items-center gap-3 min-w-0">
                <FileIcon size={20} className="text-gray-400 shrink-0" />
                <div className="min-w-0">
                    <p className="text-sm font-medium text-gray-900 truncate">{file.name}</p>
                    <p className="text-xs text-gray-500">{formatSize(file.sizeBytes)} &middot; {formatDate(file.createdAt)}</p>
                </div>
            </div>
            <div className="flex items-center gap-2 shrink-0">
                <button
                    onClick={handleDownload}
                    disabled={downloading}
                    className="p-2 text-gray-500 hover:text-blue-600 disabled:opacity-50 cursor-pointer"
                    title="Descargar"
                >
                    <Download size={18} />
                </button>
                <button
                    onClick={handleDelete}
                    disabled={deleting}
                    className="p-2 text-gray-500 hover:text-red-600 disabled:opacity-50 cursor-pointer"
                    title="Eliminar"
                >
                    <Trash2 size={18} />
                </button>
            </div>
        </div>
    );
}
