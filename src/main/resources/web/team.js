document.getElementById("addPokemonForm").addEventListener("submit", function(event) {
    event.preventDefault();

    const pokemonName = document.getElementById("pokemonName").value;
    const pokemonLevel = document.getElementById("pokemonLevel").value;

    fetch('http://localhost:35000', {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json'
        },
        body: JSON.stringify({
            name: pokemonName,
            level: pokemonLevel
        })
    })
    .then(response => response.text())
    .then(data => {
        console.log(data);
        alert("¡Pokémon agregado al equipo!");
        loadTeam();
    })
    .catch(error => console.error('Error:', error));
});

function loadTeam() {
    fetch('http://localhost:35000/team')
        .then(response => response.json())
        .then(team => {
            const teamContainer = document.getElementById("teamContainer");
            teamContainer.innerHTML = "";
            team.forEach(pokemon => {
                const pokemonDiv = document.createElement("div");
                pokemonDiv.innerHTML = `<p>${pokemon.name} - Nivel ${pokemon.level}</p>`;
                teamContainer.appendChild(pokemonDiv);
            });
        })
        .catch(error => console.error('Error:', error));
}

loadTeam();
