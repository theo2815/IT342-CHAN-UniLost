import React from "react";
import { Search } from "lucide-react";
import "./Footer.css";

function Footer() {
  return (
    <footer className="main-footer">
      <div className="footer-content">
        <div className="footer-brand">
          <div className="footer-logo">
            <Search size={20} className="text-primary" />
            <h2>UniLost</h2>
          </div>
          <p>Connecting Cebu's Academic Community.</p>
        </div>
        <div className="footer-links">
          <a href="#">Privacy Policy</a>
          <a href="#">Terms of Service</a>
          <a href="#">Contact Support</a>
        </div>
        <div className="footer-copyright">© 2026 UniLost Inc.</div>
      </div>
    </footer>
  );
}

export default Footer;
