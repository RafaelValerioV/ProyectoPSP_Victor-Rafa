function formatSize(bytes) {
    if (bytes < 1024) return bytes + ' B';
    if (bytes < 1024 * 1024) return (bytes / 1024).toFixed(1) + ' KB';
    return (bytes / (1024 * 1024)).toFixed(1) + ' MB';
}

export default function StorageBar({ label, used, limit }) {
    const usedFmt = formatSize(used || 0);
    const limitFmt = formatSize(limit || 0);
    const percentage = limit > 0 ? Math.min((used / limit) * 100, 100) : 0;

    return (
        <div className="flex items-center gap-1.5">
            <span className="text-xs text-gray-500 whitespace-nowrap">{label ? `${label}: ` : ''}{usedFmt} / {limitFmt}</span>
            <div className="w-20 h-1.5 bg-gray-200 rounded-full overflow-hidden">
                <div
                    className={`h-full rounded-full transition-all ${percentage > 90 ? 'bg-red-500' : 'bg-blue-500'}`}
                    style={{ width: `${percentage}%` }}
                />
            </div>
        </div>
    );
}
