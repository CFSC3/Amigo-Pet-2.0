const functions = require("firebase-functions/v1");
const admin = require("firebase-admin");
admin.initializeApp();

// Gatilho para Notificações de Chat
exports.notificarNovaMensagem = functions.database.ref('/banco-dados/Mensagens/{idSalaChat}/{idMensagem}')
    .onCreate(async (snapshot, context) => {
        const mensagem = snapshot.val();
        const idRemetente = mensagem.idRemetente;
        const idSalaChat = context.params.idSalaChat;

        if (!idRemetente) return null;

        const uids = idSalaChat.split('_');
        const idDestinatario = uids[0] === idRemetente ? uids[1] : uids[0];

        try {
            const destinatarioSnap = await admin.database().ref(`/banco-dados/usuario/${idDestinatario}`).once('value');
            const remetenteSnap = await admin.database().ref(`/banco-dados/usuario/${idRemetente}`).once('value');

            const tokenFCM = destinatarioSnap.val().tokenFCM;
            const nomeRemetente = remetenteSnap.val().nome || "Novo Usuário";

            if (!tokenFCM) return null;

            let corpoNotificacao = mensagem.texto;
            if (mensagem.tipoMensagem === "imagem") corpoNotificacao = "📷 Foto";
            if (mensagem.tipoMensagem === "video") corpoNotificacao = "🎥 Vídeo";

            const payload = {
                token: tokenFCM,
                data: {
                    titulo: nomeRemetente,
                    corpo: corpoNotificacao,
                    Acao: "AbrirConversa",
                    uidDestino: idRemetente
                }
            };

            return admin.messaging().send(payload);
        } catch (error) {
            console.error("Erro no push de chat:", error);
            return null;
        }
    });

// Gatilho para Notificações de Comentários
exports.notificarNovoComentario = functions.database.ref('/banco-dados/Comentarios/{idPostagem}/{idComentario}')
    .onCreate(async (snapshot, context) => {
        const comentario = snapshot.val();
        const idPostagem = context.params.idPostagem;
        const idRemetente = comentario.idUsuario;

        if (!idRemetente) return null;

        try {
            const postSnap = await admin.database().ref(`/banco-dados/Postagens/${idPostagem}`).once('value');
            if (!postSnap.exists()) return null;

            const idDonoPost = postSnap.val().idUsuario;
            if (idDonoPost === idRemetente) return null;

            const donoSnap = await admin.database().ref(`/banco-dados/usuario/${idDonoPost}`).once('value');
            const remetenteSnap = await admin.database().ref(`/banco-dados/usuario/${idRemetente}`).once('value');

            const tokenFCM = donoSnap.val().tokenFCM;
            const nomeRemetente = remetenteSnap.val().nome || "Alguém";

            if (!tokenFCM) return null;

            const payload = {
                token: tokenFCM,
                // A chave 'notification' foi removida. Tudo vai dentro de 'data'
                data: {
                    titulo: `${nomeRemetente} comentou na sua publicação`,
                    corpo: comentario.texto || "Novo comentário.",
                    Acao: "AbrirComentario",
                    idPostagem: idPostagem
                }
            };

            return admin.messaging().send(payload);
        } catch (error) {
            console.error("Erro no push de comentário:", error);
            return null;
        }
    });