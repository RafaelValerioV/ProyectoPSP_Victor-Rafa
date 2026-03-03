import { useState, useEffect } from 'react';
import toast from 'react-hot-toast';
import Layout from '../components/Layout';
import FileList from '../components/FileList';
import UploadButton from '../components/UploadButton';
import * as filesApi from '../api/files';

export default function HomePage() {
    const [files, setFiles] = useState([]);
    const [loading, setLoading] = useState(true);

    async function loadFiles() {
        try {
            const res = await filesApi.list();
            setFiles(res.data);
        } catch {
            toast.error('Error al cargar los archivos');
        } finally {
            setLoading(false);
        }
    }

    useEffect(() => {
        loadFiles();
    }, []);

    return (
        <Layout>
            <div className="flex items-center justify-between mb-6">
                <h2 className="text-lg font-semibold text-gray-900">Mis archivos</h2>
                <UploadButton onUploaded={loadFiles} />
            </div>

            {loading ? (
                <p className="text-center text-gray-500 py-12">Cargando archivos...</p>
            ) : (
                <FileList files={files} onDeleted={loadFiles} />
            )}
        </Layout>
    );
}
