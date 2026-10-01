export function Icon({ name, size = 20 }) {
  const common = {
    width: size,
    height: size,
    viewBox: '0 0 24 24',
    fill: 'none',
    stroke: 'currentColor',
    strokeWidth: 1.8,
    strokeLinecap: 'round',
    strokeLinejoin: 'round',
    'aria-hidden': true,
  }

  const paths = {
    car: <><path d="M5 17h14l1-5-2-5H6l-2 5 1 5Z"/><path d="M7 17v2M17 17v2M4 12h16M8 9h8"/></>,
    user: <><circle cx="12" cy="8" r="3"/><path d="M5 20c.8-4 3.1-6 7-6s6.2 2 7 6"/></>,
    cart: <><path d="M3 4h2l2 11h10l2-7H6"/><circle cx="9" cy="19" r="1"/><circle cx="17" cy="19" r="1"/></>,
    admin: <><rect x="4" y="4" width="16" height="16" rx="2"/><path d="M8 9h8M8 13h3M8 17h5"/></>,
    search: <><circle cx="11" cy="11" r="7"/><path d="m20 20-4-4"/></>,
    store: <><path d="M4 10h16l-2-5H6l-2 5Z"/><path d="M5 10v9h14v-9M9 19v-5h6v5"/></>,
    users: <><circle cx="9" cy="8" r="3"/><path d="M3 19c.7-3.5 2.8-5.5 6-5.5s5.3 2 6 5.5"/><path d="M15 6.5a3 3 0 0 1 0 5.5M16.5 14c2.5.3 4 2 4.5 5"/></>,
    calendar: <><rect x="3" y="5" width="18" height="16" rx="2"/><path d="M8 3v4M16 3v4M3 10h18"/></>,
    tag: <><path d="M20 13 13 20 4 11V4h7l9 9Z"/><circle cx="8.5" cy="8.5" r="1"/></>,
    edit: <><path d="m4 20 4.5-1 10-10-3.5-3.5-10 10L4 20Z"/><path d="m13.5 7 3.5 3.5"/></>,
    trash: <><path d="M4 7h16M9 7V4h6v3M7 7l1 13h8l1-13M10 11v5M14 11v5"/></>,
    plus: <path d="M12 5v14M5 12h14"/>,
    refresh: <><path d="M20 11a8 8 0 1 0-2.3 5.7"/><path d="M20 4v7h-7"/></>,
    logout: <><path d="M10 5H5v14h5M14 8l4 4-4 4M8 12h10"/></>,
    check: <path d="m5 12 4 4L19 6"/>,
    close: <><path d="m6 6 12 12M18 6 6 18"/></>,
    info: <><circle cx="12" cy="12" r="9"/><path d="M12 11v5M12 8h.01"/></>,
    menu: <><path d="M4 7h16M4 12h16M4 17h16"/></>,
    home: <><path d="m3 11 9-8 9 8"/><path d="M5 10v10h14V10M9 20v-6h6v6"/></>,
    arrow: <path d="m9 18 6-6-6-6"/>,
  }

  return <svg {...common}>{paths[name] || paths.info}</svg>
}
