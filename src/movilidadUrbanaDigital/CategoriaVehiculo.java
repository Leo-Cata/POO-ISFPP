package movilidadUrbanaDigital;

public enum CategoriaVehiculo {
	ESTANDAR(1),
	CONFORT (2),
	PREMIUM (3);
	
	private int valor;
	private CategoriaVehiculo(int valor) {
		this.valor = valor;
	}
	public int getValor() {
		return valor;
	}
	public void setValor(int valor) {
		this.valor = valor;
	}

}
