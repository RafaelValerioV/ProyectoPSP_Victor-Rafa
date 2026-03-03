import { FolderOpen } from 'lucide-react';
import FileItem from './FileItem';

export default function FileList({ files, onDeleted }) {
    if (files.length === 0) {
        return (
            <div className="text-center py-12 text-gray-400">
                <FolderOpen size={48} className="mx-auto mb-3" />
                <p>No tienes archivos todavia</p>
                <p className="text-sm">Sube tu primer archivo con el boton de arriba</p>
            </div>
        );
    }

    return (
        <div className="flex flex-col gap-2">
            {files.map((file) => (
                <FileItem key={file.id} file={file} onDeleted={onDeleted} />
            ))}
        </div>
    );
}
