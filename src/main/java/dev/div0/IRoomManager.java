package dev.div0;

import org.kurento.client.MediaElement;
import org.kurento.client.MediaPipeline;
import org.kurento.client.MediaType;
import org.kurento.room.RoomManager;
import org.kurento.room.api.KurentoClientSessionInfo;
import org.kurento.room.api.MutedMediaType;
import org.kurento.room.api.pojo.ParticipantRequest;
import org.kurento.room.api.pojo.UserParticipant;
import org.kurento.room.exception.RoomException;

import java.util.Set;

public interface IRoomManager {
    void joinRoom(String userName, String roomName, boolean dataChannels, boolean webParticipant, ParticipantRequest request);
    void leaveRoom(ParticipantRequest request);
    void publishMedia(ParticipantRequest request, boolean isOffer, String sdp,
                      MediaElement loopbackAlternativeSrc, MediaType loopbackConnectionType, boolean doLoopback,
                      MediaElement... mediaElements);
    void publishMedia(ParticipantRequest request, String sdpOffer, boolean doLoopback, MediaElement... mediaElements);
    void unpublishMedia(ParticipantRequest request);
    void subscribe(String remoteName, String sdpOffer, ParticipantRequest request);
    void unsubscribe(String remoteName, ParticipantRequest request);
    void onIceCandidate(String endpointName, String candidate, int sdpMLineIndex,
                        String sdpMid, ParticipantRequest request);
    void sendMessage(String message, String userName, String roomName,
                     ParticipantRequest request);
    int totalParticipants(String roomName);
    void evictParticipant(String participantId) throws RoomException;
    void closeRoom(String roomName) throws RoomException;
    MediaPipeline getPipeline(String participantId) throws RoomException;
    void createRoom(KurentoClientSessionInfo kcSessionInfo) throws RoomException;
    Set<UserParticipant> getPeerSubscribers(String participantId) throws RoomException;
    Set<UserParticipant> getPeerPublishers(String participantId) throws RoomException;
    Set<UserParticipant> getSubscribers(String roomName) throws RoomException;
    Set<UserParticipant> getPublishers(String roomName) throws RoomException;
    Set<UserParticipant> getParticipants(String roomName) throws RoomException;
    Set<String> getRooms();
    void close();
    String generatePublishOffer(String participantId) throws RoomException;
    void addMediaElement(String participantId, MediaElement element) throws RoomException;
    void addMediaElement(String participantId, MediaElement element, MediaType type) throws RoomException;
    void removeMediaElement(String participantId, MediaElement element) throws RoomException;
    void mutePublishedMedia(MutedMediaType muteType, String participantId) throws RoomException;
    void unmutePublishedMedia(String participantId) throws RoomException;
    void unmuteSubscribedMedia(String remoteName, String participantId) throws RoomException;
    RoomManager getRoomManager();

}
