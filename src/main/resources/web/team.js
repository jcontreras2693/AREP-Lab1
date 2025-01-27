const form = document.getElementById('addPokemonForm');
const teamContainer = document.getElementById('teamContainer');

form.addEventListener('submit', (event) => {
    event.preventDefault();
    const pokemonName = document.getElementById('pokemonName').value;

    if (pokemonName) {
        const card = document.createElement('div');
        card.classList.add('pokemon-card');
        card.textContent = pokemonName;

        teamContainer.appendChild(card);
        form.reset();
    }
});
