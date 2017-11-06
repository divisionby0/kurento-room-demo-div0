package dev.div0;

import org.kurento.client.MediaElement;
import org.kurento.client.MediaPipeline;
import org.kurento.client.MediaType;
import org.kurento.room.RoomManager;
import org.kurento.room.api.*;
import org.kurento.room.api.pojo.ParticipantRequest;
import org.kurento.room.api.pojo.UserParticipant;
import org.kurento.room.exception.RoomException;
import org.kurento.room.internal.DefaultKurentoClientSessionInfo;
import org.kurento.room.internal.DefaultNotificationRoomHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PreDestroy;
import java.util.Set;

// copy of NotificationRoomManager
// i cant get private internalManager(and its right) to extend NotificationRoomManager with custom joinRoom method
public class NotificationRoomManager1 {
    private final Logger log = LoggerFactory.getLogger(NotificationRoomManager1.class);

    private NotificationRoomHandler notificationRoomHandler;
    private RoomManager internalManager;

    public NotificationRoomManager1(UserNotificationService notificationService,
                                    KurentoClientProvider kcProvider) {
        super();
        this.notificationRoomHandler = new DefaultNotificationRoomHandler(notificationService);
        this.internalManager = new RoomManager(notificationRoomHandler, kcProvider);
    }

    public NotificationRoomManager1(NotificationRoomHandler notificationRoomHandler,
                                    KurentoClientProvider kcProvider) {
        super();
        this.notificationRoomHandler = notificationRoomHandler;
        this.internalManager = new RoomManager(notificationRoomHandler, kcProvider);
    }

    // ----------------- CLIENT-ORIGINATED REQUESTS ------------

    public void joinRoom(String userName, String roomName, boolean dataChannels,
                         boolean webParticipant, ParticipantRequest request) {
        Set<UserParticipant> existingParticipants = null;
        try {
            KurentoClientSessionInfo kcSessionInfo = new DefaultKurentoClientSessionInfo(
                    request.getParticipantId(), roomName);
            existingParticipants = internalManager.joinRoom(userName, roomName, dataChannels,
                    webParticipant, kcSessionInfo, request.getParticipantId());
        } catch (RoomException e) {
            log.warn("PARTICIPANT {}: Error joining/creating room {}", userName, roomName, e);
            notificationRoomHandler.onParticipantJoined(request, roomName, userName, null, e);
        }
        if (existingParticipants != null) {
            notificationRoomHandler.onParticipantJoined(request, roomName, userName,
                    existingParticipants, null);
        }
    }

    public void leaveRoom(ParticipantRequest request) {
        String pid = request.getParticipantId();
        Set<UserParticipant> remainingParticipants = null;
        String roomName = null;
        String userName = null;
        try {
            roomName = internalManager.getRoomName(pid);
            userName = internalManager.getParticipantName(pid);
            remainingParticipants = internalManager.leaveRoom(pid);
        } catch (RoomException e) {
            log.warn("PARTICIPANT {}: Error leaving room {}", userName, roomName, e);
            notificationRoomHandler.onParticipantLeft(request, null, null, e);
        }
        if (remainingParticipants != null) {
            notificationRoomHandler.onParticipantLeft(request, userName, remainingParticipants, null);
        }
    }

    public void publishMedia(ParticipantRequest request, boolean isOffer, String sdp,
                             MediaElement loopbackAlternativeSrc, MediaType loopbackConnectionType, boolean doLoopback,
                             MediaElement... mediaElements) {
        String pid = request.getParticipantId();
        String userName = null;
        Set<UserParticipant> participants = null;
        String sdpAnswer = null;
        try {
            userName = internalManager.getParticipantName(pid);
            sdpAnswer = internalManager.publishMedia(request.getParticipantId(), isOffer, sdp,
                    loopbackAlternativeSrc, loopbackConnectionType, doLoopback, mediaElements);
            participants = internalManager.getParticipants(internalManager.getRoomName(pid));
        } catch (RoomException e) {
            log.warn("PARTICIPANT {}: Error publishing media", userName, e);
            notificationRoomHandler.onPublishMedia(request, null, null, null, e);
        }
        if (sdpAnswer != null) {
            notificationRoomHandler.onPublishMedia(request, userName, sdpAnswer, participants, null);
        }
    }

    public void publishMedia(ParticipantRequest request, String sdpOffer, boolean doLoopback,
                             MediaElement... mediaElements) {
        this.publishMedia(request, true, sdpOffer, null, null, doLoopback, mediaElements);
    }

    public void unpublishMedia(ParticipantRequest request) {
        String pid = request.getParticipantId();
        String userName = null;
        Set<UserParticipant> participants = null;
        boolean unpublished = false;
        try {
            userName = internalManager.getParticipantName(pid);
            internalManager.unpublishMedia(pid);
            unpublished = true;
            participants = internalManager.getParticipants(internalManager.getRoomName(pid));
        } catch (RoomException e) {
            log.warn("PARTICIPANT {}: Error unpublishing media", userName, e);
            notificationRoomHandler.onUnpublishMedia(request, null, null, e);
        }
        if (unpublished) {
            notificationRoomHandler.onUnpublishMedia(request, userName, participants, null);
        }
    }

    public void subscribe(String remoteName, String sdpOffer, ParticipantRequest request) {
        String pid = request.getParticipantId();
        String userName = null;
        String sdpAnswer = null;
        try {
            userName = internalManager.getParticipantName(pid);
            sdpAnswer = internalManager.subscribe(remoteName, sdpOffer, pid);
        } catch (RoomException e) {
            log.warn("PARTICIPANT {}: Error subscribing to {}", userName, remoteName, e);
            notificationRoomHandler.onSubscribe(request, null, e);
        }
        if (sdpAnswer != null) {
            notificationRoomHandler.onSubscribe(request, sdpAnswer, null);
        }
    }

    public void unsubscribe(String remoteName, ParticipantRequest request) {
        String pid = request.getParticipantId();
        String userName = null;
        boolean unsubscribed = false;
        try {
            userName = internalManager.getParticipantName(pid);
            internalManager.unsubscribe(remoteName, pid);
            unsubscribed = true;
        } catch (RoomException e) {
            log.warn("PARTICIPANT {}: Error unsubscribing from {}", userName, remoteName, e);
            notificationRoomHandler.onUnsubscribe(request, e);
        }
        if (unsubscribed) {
            notificationRoomHandler.onUnsubscribe(request, null);
        }
    }

    public void onIceCandidate(String endpointName, String candidate, int sdpMLineIndex,
                               String sdpMid, ParticipantRequest request) {
        String pid = request.getParticipantId();
        String userName = null;
        try {
            userName = internalManager.getParticipantName(pid);
            internalManager.onIceCandidate(endpointName, candidate, sdpMLineIndex, sdpMid,
                    request.getParticipantId());
            notificationRoomHandler.onRecvIceCandidate(request, null);
        } catch (RoomException e) {
            log.warn("PARTICIPANT {}: Error receiving ICE " + "candidate (epName={}, candidate={})",
                    userName, endpointName, candidate, e);
            notificationRoomHandler.onRecvIceCandidate(request, e);
        }
    }

    public void sendMessage(String message, String userName, String roomName,
                            ParticipantRequest request) {
        log.debug("Request [SEND_MESSAGE] message={} ({})", message, request);
        try {
            if (!internalManager.getParticipantName(request.getParticipantId()).equals(userName)) {
                throw new RoomException(RoomException.Code.USER_NOT_FOUND_ERROR_CODE, "Provided username '" + userName
                        + "' differs from the participant's name");
            }
            if (!internalManager.getRoomName(request.getParticipantId()).equals(roomName)) {
                throw new RoomException(RoomException.Code.ROOM_NOT_FOUND_ERROR_CODE, "Provided room name '" + roomName
                        + "' differs from the participant's room");
            }
            notificationRoomHandler.onSendMessage(request, message, userName, roomName,
                    internalManager.getParticipants(roomName), null);
        } catch (RoomException e) {
            log.warn("PARTICIPANT {}: Error sending message", userName, e);
            notificationRoomHandler.onSendMessage(request, null, null, null, null, e);
        }
    }

    // ----------------- APPLICATION-ORIGINATED REQUESTS ------------
    @PreDestroy
    public void close() {
        if (!internalManager.isClosed()) {
            internalManager.close();
        }
    }

    public Set<String> getRooms() {
        return internalManager.getRooms();
    }

    public Set<UserParticipant> getParticipants(String roomName) throws RoomException {
        return internalManager.getParticipants(roomName);
    }

    public Set<UserParticipant> getPublishers(String roomName) throws RoomException {
        return internalManager.getPublishers(roomName);
    }

    public Set<UserParticipant> getSubscribers(String roomName) throws RoomException {
        return internalManager.getSubscribers(roomName);
    }

    public Set<UserParticipant> getPeerPublishers(String participantId) throws RoomException {
        return internalManager.getPeerPublishers(participantId);
    }

    public Set<UserParticipant> getPeerSubscribers(String participantId) throws RoomException {
        return internalManager.getPeerSubscribers(participantId);
    }

    public void createRoom(KurentoClientSessionInfo kcSessionInfo) throws RoomException {
        internalManager.createRoom(kcSessionInfo);
    }

    public MediaPipeline getPipeline(String participantId) throws RoomException {
        return internalManager.getPipeline(participantId);
    }

    public void evictParticipant(String participantId) throws RoomException {
        UserParticipant participant = internalManager.getParticipantInfo(participantId);
        Set<UserParticipant> remainingParticipants = internalManager.leaveRoom(participantId);
        notificationRoomHandler.onParticipantLeft(participant.getUserName(), remainingParticipants);
        notificationRoomHandler.onParticipantEvicted(participant);
    }

    public void closeRoom(String roomName) throws RoomException {
        Set<UserParticipant> participants = internalManager.closeRoom(roomName);
        notificationRoomHandler.onRoomClosed(roomName, participants);
    }

    public String generatePublishOffer(String participantId) throws RoomException {
        return internalManager.generatePublishOffer(participantId);
    }

    public void addMediaElement(String participantId, MediaElement element) throws RoomException {
        internalManager.addMediaElement(participantId, element);
    }

    public void addMediaElement(String participantId, MediaElement element, MediaType type)
            throws RoomException {
        internalManager.addMediaElement(participantId, element, type);
    }

    public void removeMediaElement(String participantId, MediaElement element) throws RoomException {
        internalManager.removeMediaElement(participantId, element);
    }

    public void mutePublishedMedia(MutedMediaType muteType, String participantId)
            throws RoomException {
        internalManager.mutePublishedMedia(muteType, participantId);
    }

    public void unmutePublishedMedia(String participantId) throws RoomException {
        internalManager.unmutePublishedMedia(participantId);
    }

    public void muteSubscribedMedia(String remoteName, MutedMediaType muteType, String participantId)
            throws RoomException {
        internalManager.muteSubscribedMedia(remoteName, muteType, participantId);
    }

    public void unmuteSubscribedMedia(String remoteName, String participantId) throws RoomException {
        internalManager.unmuteSubscribedMedia(remoteName, participantId);
    }

    public RoomManager getRoomManager() {
        return internalManager;
    }

    protected Set<UserParticipant> internalJoinRoom(String userName, String roomName, boolean dataChannels, boolean webParticipant, KurentoClientSessionInfo kcSessionInfo, String participantId){
        Set<UserParticipant> existingParticipants = internalManager.joinRoom(userName, roomName, dataChannels, webParticipant, kcSessionInfo, participantId);
        return existingParticipants;
    }
    protected void handleParticipantJoined(ParticipantRequest request, String roomName, String newUserName, Set<UserParticipant> existingParticipants, RoomException error){
        notificationRoomHandler.onParticipantJoined(request, roomName, newUserName, existingParticipants, error);
    }
}
