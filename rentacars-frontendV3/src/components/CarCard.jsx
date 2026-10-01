import { moneda } from '../utils/format'
import { Icon } from './Icons'
import './CarCard.css'

export default function CarCard({ auto, onRent }) {
  const oferta = Number(auto.oferta_porcentaje || 0)
  const precio = Number(auto.precio_con_oferta ?? auto.precio_dia * (1 - oferta / 100))

  return (
    <article className="car-card">
      <div className="car-card__visual">
        {oferta > 0 && <span className="discount-badge">-{oferta}%</span>}
        <div className="car-illustration" aria-hidden="true"><Icon name="car" size={88} /></div>
        <span className="car-card__id">ID #{auto.id_auto}</span>
      </div>
      <div className="car-card__body">
        <p className="eyebrow">{auto.marca || 'Vehículo'}</p>
        <h3>{auto.modelo || `Auto ${auto.id_auto}`}</h3>
        <div className="availability"><span /> Disponible para reservar</div>
        <div className="price-block">
          {oferta > 0 && <small>Antes: <s>{moneda.format(Number(auto.precio_dia))}</s></small>}
          <strong>{moneda.format(precio)}</strong>
          <span>por día</span>
        </div>
        <button className="btn btn-primary btn-block" type="button" onClick={() => onRent(auto)}>
          Alquilar este auto
        </button>
      </div>
    </article>
  )
}
