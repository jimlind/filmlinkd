declare module 'markdown-truncate' {
    export default function truncateMarkdown(
        inputText: string,
        options: { limit: number; ellipsis: boolean },
    ): string;
}
