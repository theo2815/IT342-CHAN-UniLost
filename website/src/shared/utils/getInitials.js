export function getInitials(name) {
    if (!name) return '??';
    return name.split(' ').map(p => p.charAt(0)).join('').substring(0, 2).toUpperCase();
}
