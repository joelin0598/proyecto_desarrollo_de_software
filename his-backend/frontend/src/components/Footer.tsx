function Footer() {
  const currentYear = new Date().getFullYear()

  return (
    <footer className="bg-gray-800 text-gray-300 py-12">
      <div className="container-main">
        <div className="grid grid-cols-1 md:grid-cols-3 lg:grid-cols-4 gap-8 mb-8">
          {/* About */}
          <div>
            <h3 className="text-white font-bold mb-4 text-lg">HIS</h3>
            <p className="text-sm">
              Sistema de Información Hospitalario para gestionar tu salud de forma segura y remota.
            </p>
          </div>

          {/* Services */}
          <div>
            <h3 className="text-white font-bold mb-4">Servicios</h3>
            <ul className="space-y-2 text-sm">
              <li><a href="#" className="hover:text-white transition">Agendar Cita</a></li>
              <li><a href="#" className="hover:text-white transition">Laboratorio</a></li>
              <li><a href="#" className="hover:text-white transition">Recetas</a></li>
              <li><a href="#" className="hover:text-white transition">Historial Médico</a></li>
            </ul>
          </div>

          {/* Company */}
          <div>
            <h3 className="text-white font-bold mb-4">Compañía</h3>
            <ul className="space-y-2 text-sm">
              <li><a href="#" className="hover:text-white transition">Acerca de</a></li>
              <li><a href="#" className="hover:text-white transition">Contacto</a></li>
              <li><a href="#" className="hover:text-white transition">Blog</a></li>
              <li><a href="#" className="hover:text-white transition">Carreras</a></li>
            </ul>
          </div>

          {/* Contact */}
          <div>
            <h3 className="text-white font-bold mb-4">Contacto</h3>
            <ul className="space-y-2 text-sm">
              <li>📧 info@hospital.com</li>
              <li>📞 +502 7777-8888</li>
              <li>📍 Guatemala City, GT</li>
              <li>⏰ Lunes - Viernes, 8AM - 6PM</li>
            </ul>
          </div>
        </div>

        {/* Bottom */}
        <div className="border-t border-gray-700 pt-8 text-center text-sm">
          <p>&copy; {currentYear} Hospital Information System. Todos los derechos reservados.</p>
          <div className="mt-4 space-x-6">
            <a href="#" className="hover:text-white transition">Privacidad</a>
            <a href="#" className="hover:text-white transition">Términos</a>
            <a href="#" className="hover:text-white transition">Cookies</a>
          </div>
        </div>
      </div>
    </footer>
  )
}

export default Footer

