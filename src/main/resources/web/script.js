document.getElementById('addPokemonForm').addEventListener('submit', async (e) => {
    e.preventDefault();

    const name = document.getElementById('pokemonName').value;
    const level = parseInt(document.getElementById('pokemonLevel').value);

    // Enviar POST al servidor
    const response = await fetch('http://localhost:35000/api/pokemon', {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json'
        },
        body: JSON.stringify({ name, level })
    });

    if (response.ok) {
        updateTeamList();
    } else {
        alert('Error al agregar el Pokémon');
    }
});
async function updateTeamList() {
    // Obtener lista actualizada
    const response = await fetch('http://localhost:35000/api/pokemon');
    const team = await response.json();

    const container = document.getElementById('teamContainer');
    container.innerHTML = team.map(p => `
        <div class="pokemon-card">
            <h3>${p.name}</h3>
            <p>Nivel: ${p.level}</p>
        </div>
    `).join('');
}

// Cargar equipo al iniciar
updateTeamList();